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
)
