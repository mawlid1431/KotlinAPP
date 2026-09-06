package com.aiu.tdminsight.data.supabase

import com.aiu.tdminsight.data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SupabaseModels — wire-format DTOs matching the PostgreSQL schema in supabase/schema.sql.
 * Kotlin model → DTO → Supabase; results come back as DTO → domain model.
 */

@Serializable
data class CaseDto(
    // Primary key is assigned by the database (gen_random_uuid).
    val id: String? = null,

    // Ownership — set to the Clerk user ID extracted from the JWT.
    @SerialName("user_id") val userId: String,

    // Patient / dosing metadata
    @SerialName("case_label")              val caseLabel: String,
    val workflow: String,
    @SerialName("weight_kg")               val weightKg: Double,
    @SerialName("age_years")               val ageYears: Int,
    @SerialName("is_male")                 val isMale: Boolean,
    @SerialName("scr_umol_l")             val scrUmolL: Double,
    @SerialName("dose_mg")                 val doseMg: Double,
    @SerialName("interval_hours")          val intervalHours: Double,
    @SerialName("infusion_duration_hours") val infusionDurationHours: Double,

    // Concentration samples (null when the workflow does not require the sample)
    @SerialName("pre_conc_mg_l")           val preConcMgL: Double? = null,
    @SerialName("pre_time_h")              val preTimeH: Double? = null,
    @SerialName("post_conc_mg_l")          val postConcMgL: Double? = null,
    @SerialName("post_time_h")             val postTimeH: Double? = null,

    // PK results
    @SerialName("ke_per_hour")             val kePerHour: Double? = null,
    @SerialName("half_life_hours")         val halfLifeHours: Double? = null,
    @SerialName("vd_l")                    val vdL: Double? = null,
    @SerialName("vd_l_per_kg")            val vdLPerKg: Double? = null,
    @SerialName("clearance_l_per_hour")   val clearanceLPerHour: Double? = null,
    @SerialName("auc24")                   val auc24: Double? = null,
    @SerialName("recommended_dose_mg")     val recommendedDoseMg: Double? = null,
    @SerialName("c_min")                   val cmin: Double? = null,
    @SerialName("c_max")                   val cmax: Double? = null,

    // Timestamps — assigned by the database, read-only on insert
    @SerialName("created_at") val createdAt: String? = null,
)

// ── Conversion helpers ────────────────────────────────────────────────────────

internal fun CaseDto.toHistoryEntry() = HistoryEntry(
    caseId    = caseLabel,
    date      = createdAt?.take(10) ?: "—",
    workflow  = when (workflow) {
        "POST"     -> VancoWorkflow.POST
        "PRE_POST" -> VancoWorkflow.PRE_POST
        else       -> VancoWorkflow.PRE
    },
    doseMg    = doseMg,
    intervalH = intervalHours,
    tInfH     = infusionDurationHours,
    auc24     = auc24 ?: 0.0,
    recDoseMg = recommendedDoseMg ?: 0.0,
    ke        = kePerHour ?: 0.0,
    t12       = halfLifeHours ?: 0.0,
    vdL       = vdL ?: 0.0,
    clLH      = clearanceLPerHour ?: 0.0,

    // Full detail — read back so the detail screen and PDF export have the
    // complete record instead of only the summary fields above.
    rowId        = id,
    createdAtIso = createdAt,
    weightKg     = weightKg,
    ageYears     = ageYears,
    isMale       = isMale,
    scrUmolL     = scrUmolL,
    vdLPerKg     = vdLPerKg ?: 0.0,
    preConcMgL   = preConcMgL,
    preTimeH     = preTimeH,
    postConcMgL  = postConcMgL,
    postTimeH    = postTimeH,
    cmin         = cmin,
    cmax         = cmax,
)


// ── user_profiles ─────────────────────────────────────────────────────────────

/**
 * Full row of `user_profiles`, used when READING a profile back.
 * Every field is optional except the Clerk user ID, because a row may have
 * been created by the ensure_user_profile() trigger before the app ever
 * synced identity fields into it.
 */
@Serializable
data class UserProfileDto(
    @SerialName("user_id")      val userId: String,
    val email: String? = null,
    @SerialName("first_name")   val firstName: String? = null,
    @SerialName("last_name")    val lastName: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url")   val avatarUrl: String? = null,
    val institution: String? = null,
    val department: String? = null,
    val role: String? = null,
)

/**
 * WRITE payload for the Clerk -> Supabase sync.
 *
 * Deliberately narrower than [UserProfileDto]: it carries only the columns
 * Clerk owns. On an upsert conflict PostgREST updates just these columns, so
 * institution / department / role survive every subsequent login untouched.
 */
@Serializable
data class UserProfileSyncDto(
    @SerialName("user_id")      val userId: String,
    val email: String,
    @SerialName("first_name")   val firstName: String? = null,
    @SerialName("last_name")    val lastName: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url")   val avatarUrl: String? = null,
)

internal fun UserProfileDto.toUserProfile() = UserProfile(
    userId      = userId,
    email       = email.orEmpty(),
    firstName   = firstName,
    lastName    = lastName,
    displayName = displayName,
    avatarUrl   = avatarUrl,
    institution = institution,
    department  = department,
    role        = role,
)
