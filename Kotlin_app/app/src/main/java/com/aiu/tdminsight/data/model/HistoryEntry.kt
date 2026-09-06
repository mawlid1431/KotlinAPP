package com.aiu.tdminsight.data.model

/**
 * A saved calculation, as shown on the Home and History screens.
 *
 * This is a domain model, not a UI type: it is produced by the data layer
 * (SupabaseRepository maps CaseDto -> HistoryEntry) and consumed by the UI.
 * It lives here so the data layer never has to import a UI package.
 */
data class HistoryEntry(
    val caseId: String,
    val date: String,
    val workflow: VancoWorkflow,
    val doseMg: Double,
    val intervalH: Double,
    val tInfH: Double,
    val auc24: Double,
    val recDoseMg: Double,
    val ke: Double,
    val t12: Double,
    val vdL: Double,
    val clLH: Double,

    // ── Full case detail ──────────────────────────────────────────────────
    // Added so the detail screen and the PDF export can show the complete
    // record. All default, so nothing that only needs the summary changes.
    val rowId: String? = null,
    val createdAtIso: String? = null,
    val weightKg: Double = 0.0,
    val ageYears: Int = 0,
    val isMale: Boolean = true,
    val scrUmolL: Double = 0.0,
    val vdLPerKg: Double = 0.0,
    val preConcMgL: Double? = null,
    val preTimeH: Double? = null,
    val postConcMgL: Double? = null,
    val postTimeH: Double? = null,
    val cmin: Double? = null,
    val cmax: Double? = null,
) {
    /** AUC24 verdict against the 400-600 mg.h/L therapeutic band (Rybak 2020). */
    val verdict: Auc24Verdict
        get() = when {
            auc24 <= 0.0   -> Auc24Verdict.INVALID
            auc24 < 400.0  -> Auc24Verdict.BELOW_TARGET
            auc24 > 600.0  -> Auc24Verdict.ABOVE_TARGET
            else           -> Auc24Verdict.IN_TARGET
        }
}
