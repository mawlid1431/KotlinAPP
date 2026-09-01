package com.aiu.tdminsight.data.model

// ─── Pharmacokinetic intermediate + final results ─────────────────────────────
// All fields are nullable: not every workflow produces every parameter.
data class PkResults(
    // Elimination rate constant, h⁻¹
    val kePerHour: Double? = null,
    // Elimination half-life, h
    val halfLifeHours: Double? = null,
    // Volume of distribution, L/kg
    val vdLPerKg: Double? = null,
    // Absolute Vd, L
    val vdL: Double? = null,
    // Clearance, L/h
    val clearanceLPerHour: Double? = null,
    // AUC over 24 h, mg·h/L  (the primary TDM target)
    val auc24: Double? = null,
    // Recommended dose to centre AUC₂₄ on 500 mg·h/L target, mg
    val recommendedDoseMg: Double? = null,
    // Projected Cmin (trough), mg/L — POST and PRE+POST workflows
    val cmin: Double? = null,
    // Projected Cmax (peak), mg/L — POST and PRE+POST workflows
    val cmax: Double? = null,
)

// ─── Verdict chip on the Results screen ─────────────────────────────────────
enum class Auc24Verdict { IN_TARGET, BELOW_TARGET, ABOVE_TARGET, INVALID }

// ─── Result wrapper ───────────────────────────────────────────────────────────
sealed class CalculationResult {
    data class Success(
        val workflow: VancoWorkflow,
        val intermediate: PkResults,
    ) : CalculationResult()

    data class Failure(val message: String) : CalculationResult()
}
