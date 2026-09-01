package com.aiu.tdminsight.data.supabase

import android.util.Log
import com.aiu.tdminsight.auth.AuthRepository
import com.aiu.tdminsight.data.model.CalculationResult
import com.aiu.tdminsight.data.model.VancoWorkflow
import com.aiu.tdminsight.ui.screens.HistoryEntry
import com.aiu.tdminsight.viewmodel.CaseUiState
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
        private const val TABLE = "cases"
        private const val TAG   = "SupabaseRepo"
    }

    /**
     * Save a completed calculation case.
     * Returns true on success, false on any failure (calculation is unaffected either way).
     */
    suspend fun saveCase(state: CaseUiState, result: CalculationResult.Success): Boolean {
        return try {
            val userId = authRepo.savedSession()?.userId ?: "anonymous"
            val pk     = state.patient
            val ds     = state.dosing
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
                preConcMgL            = if (result.workflow != VancoWorkflow.POST)  state.pre.preDoseConcentration  else null,
                preTimeH              = if (result.workflow != VancoWorkflow.POST)  state.pre.hoursBeforeDose       else null,
                postConcMgL           = if (result.workflow != VancoWorkflow.PRE)   state.post.postDoseConcentration else null,
                postTimeH             = if (result.workflow != VancoWorkflow.PRE)   state.post.hoursAfterEndOfInfusion else null,
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
     * Filters client-side by user_id so each student only sees their own work.
     * Returns an empty list on any failure (caller falls back to demo data).
     */
    internal suspend fun loadRecentCases(limit: Int = 20): List<HistoryEntry> {
        return try {
            val userId = authRepo.savedSession()?.userId ?: return emptyList()
            supabase.from(TABLE)
                .select {
                    order("created_at", Order.DESCENDING)
                    limit((limit * 4).toLong())   // over-fetch then filter client-side
                }
                .decodeList<CaseDto>()
                .filter { it.userId == userId }
                .take(limit)
                .map { it.toHistoryEntry() }
        } catch (e: Exception) {
            Log.w(TAG, "loadRecentCases failed: ${e.message}")
            emptyList()
        }
    }
}
