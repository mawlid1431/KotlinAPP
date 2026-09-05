package com.aiu.tdminsight.data.supabase

import android.util.Log
import com.aiu.tdminsight.auth.AuthRepository
import com.aiu.tdminsight.data.model.CalculationResult
import com.aiu.tdminsight.data.model.DosingInput
import com.aiu.tdminsight.data.model.PatientInput
import com.aiu.tdminsight.data.model.PostSampleInput
import com.aiu.tdminsight.data.model.PreSampleInput
import com.aiu.tdminsight.data.model.VancoWorkflow
import com.aiu.tdminsight.data.model.HistoryEntry
import com.aiu.tdminsight.data.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

/**
 * SupabaseRepository — all application-level PostgreSQL operations.
 *
 * Rules this class enforces:
 *   - Only the anon key is ever in the Android binary (no service_role key).
 *   - Failures are caught and logged; they never crash the calculation flow.
 */
class SupabaseRepository(
    private val supabase: SupabaseClient,
    private val authRepo: AuthRepository,
) {
    companion object {
        private const val TABLE          = "cases"
        private const val PROFILE_TABLE  = "user_profiles"
        private const val TAG            = "SupabaseRepo"
    }

    /**
     * Save a completed calculation case.
     * Returns true on success, false on any failure (calculation is unaffected either way).
     */
    suspend fun saveCase(
        patient: PatientInput,
        dosing: DosingInput,
        pre: PreSampleInput,
        post: PostSampleInput,
        result: CalculationResult.Success,
    ): Boolean {
        return try {
            // Refuse rather than writing under "anonymous". loadRecentCases()
            // filters by the signed-in user's id, so an anonymous row could
            // never be read back - it would be silently orphaned data.
            val userId = authRepo.savedSession()?.userId
            if (userId == null) {
                Log.w(TAG, "saveCase skipped: no signed-in user")
                return false
            }
            val pk     = patient
            val ds     = dosing
            val r      = result.intermediate

            val dto = CaseDto(
                userId                = userId,
                caseLabel             = pk.caseId.ifBlank { "Case-${System.currentTimeMillis()}" },
                workflow              = result.workflow.name,
                weightKg              = pk.weightKg,
                ageYears              = pk.ageLYears,
                isMale                = pk.isMale,
                scrUmolL              = pk.scrUmolL,
                doseMg                = ds.doseMg,
                intervalHours         = ds.intervalHours,
                infusionDurationHours = ds.infusionDurationHours,
                preConcMgL            = if (result.workflow != VancoWorkflow.POST)  pre.preDoseConcentration  else null,
                preTimeH              = if (result.workflow != VancoWorkflow.POST)  pre.hoursBeforeDose       else null,
                postConcMgL           = if (result.workflow != VancoWorkflow.PRE)   post.postDoseConcentration else null,
                postTimeH             = if (result.workflow != VancoWorkflow.PRE)   post.hoursAfterEndOfInfusion else null,
                kePerHour             = r.kePerHour,
                halfLifeHours         = r.halfLifeHours,
                vdL                   = r.vdL,
                vdLPerKg              = r.vdLPerKg,
                clearanceLPerHour     = r.clearanceLPerHour,
                auc24                 = r.auc24,
                recommendedDoseMg     = r.recommendedDoseMg,
                cmin                  = r.cmin,
                cmax                  = r.cmax,
            )
            supabase.from(TABLE).insert(dto)
            Log.d(TAG, "Case saved: ${dto.caseLabel}")
            true
        } catch (e: Exception) {
            Log.w(TAG, "saveCase failed (Supabase or network error): ${e.message}")
            false
        }
    }

    /**
     * Fetch the 20 most recent cases for the current user.
     * Filters by user_id on PostgreSQL server so each user only receives their own work.
     * Returns an empty list on any failure (caller falls back to demo data).
     */
    internal suspend fun loadRecentCases(limit: Int = 20): List<HistoryEntry> {
        return try {
            val userId = authRepo.savedSession()?.userId ?: return emptyList()
            supabase.from(TABLE)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<CaseDto>()
                .map { it.toHistoryEntry() }
        } catch (e: Exception) {
            Log.w(TAG, "loadRecentCases failed: ${e.message}")
            emptyList()
        }
    }

    // ── User profile: Clerk identity -> Supabase ──────────────────────────

    /**
     * Creates or updates this user's row in `user_profiles`.
     *
     * Uses a single UPSERT keyed on `user_id` (the table's primary key, which
     * holds the Clerk user ID). Because it is one atomic statement:
     *   - first login  -> INSERT, one new row
     *   - every login after -> UPDATE of that same row
     * There is no read-then-write gap, so two rapid logins cannot race into
     * two rows. This is what satisfies "repeated logins must not duplicate
     * the user".
     *
     * Only Clerk-owned columns are sent, so institution / department / role
     * set inside the app or the SQL editor are never overwritten by a login.
     */
    suspend fun syncUserProfile(
        userId: String,
        email: String,
        firstName: String?,
        lastName: String?,
        displayName: String?,
        avatarUrl: String?,
    ): Boolean {
        if (userId.isBlank()) {
            Log.w(TAG, "syncUserProfile skipped: blank Clerk user id")
            return false
        }
        return try {
            val dto = UserProfileSyncDto(
                userId      = userId,
                email       = email,
                firstName   = firstName,
                lastName    = lastName,
                displayName = displayName,
                avatarUrl   = avatarUrl,
            )
            // onConflict = the primary key, so this is INSERT-or-UPDATE in one
            // atomic statement. defaultToNull = false keeps columns that are
            // absent from the payload (institution/department/role) as they are.
            supabase.from(PROFILE_TABLE).upsert(
                listOf(dto),
                onConflict = "user_id",
                defaultToNull = false,
            )
            Log.d(TAG, "Profile synced for $userId")
            true
        } catch (e: Exception) {
            Log.w(TAG, "syncUserProfile failed: ${e.message}")
            false
        }
    }

    /** Reads this user's stored profile, or null if there is no row / it failed. */
    suspend fun loadUserProfile(userId: String): UserProfile? {
        if (userId.isBlank()) return null
        return try {
            supabase.from(PROFILE_TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserProfileDto>()
                .firstOrNull()
                ?.toUserProfile()
        } catch (e: Exception) {
            Log.w(TAG, "loadUserProfile failed: ${e.message}")
            null
        }
    }

    /**
     * Deletes this user's saved cases, leaving their profile intact.
     * Filtered by user_id, so it can never touch another user's cases.
     */
    suspend fun deleteAllCases(userId: String): Boolean {
        if (userId.isBlank()) {
            Log.w(TAG, "deleteAllCases refused: blank user id")
            return false
        }
        return try {
            supabase.from(TABLE).delete { filter { eq("user_id", userId) } }
            Log.d(TAG, "Cleared saved cases for $userId")
            true
        } catch (e: Exception) {
            Log.w(TAG, "deleteAllCases failed: ${e.message}")
            false
        }
    }

    /**
     * Removes every row this user owns, for the "Delete account" flow.
     *
     * Both deletes are filtered by `eq("user_id", userId)` with the id taken
     * from the signed-in session, so this can only ever affect the caller's
     * own data — never another user's.
     */
    suspend fun deleteAllUserData(userId: String): Boolean {
        if (userId.isBlank()) {
            Log.w(TAG, "deleteAllUserData refused: blank Clerk user id")
            return false
        }
        return try {
            supabase.from(TABLE).delete { filter { eq("user_id", userId) } }
            supabase.from(PROFILE_TABLE).delete { filter { eq("user_id", userId) } }
            Log.d(TAG, "Deleted all Supabase data for $userId")
            true
        } catch (e: Exception) {
            Log.w(TAG, "deleteAllUserData failed: ${e.message}")
            false
        }
    }
}
