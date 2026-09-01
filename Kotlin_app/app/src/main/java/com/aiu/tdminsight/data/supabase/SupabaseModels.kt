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

internal fun CaseDto.toHistoryEntry() = com.aiu.tdminsight.ui.screens.HistoryEntry(
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
)
