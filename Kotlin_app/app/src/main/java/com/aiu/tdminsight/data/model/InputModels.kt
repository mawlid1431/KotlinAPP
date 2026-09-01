package com.aiu.tdminsight.data.model

// ─── Workflow selector ─────────────────────────────────────────────────────────
enum class VancoWorkflow { PRE, POST, PRE_POST }

// ─── Patient data ──────────────────────────────────────────────────────────────
data class PatientInput(
    val caseId: String    = "",
    val weightKg: Double  = 0.0,   // kg
    val heightCm: Double  = 0.0,   // cm (needed for some CrCl estimates)
    val ageLYears: Int    = 0,      // years
    val isMale: Boolean   = true,
    val scrUmolL: Double  = 0.0,   // serum creatinine µmol/L
)

// ─── Common dosing fields (shared across all workflows) ───────────────────────
data class DosingInput(
    val doseMg: Double            = 0.0,   // mg
    val intervalHours: Double     = 0.0,   // dosing interval, h
    val infusionDurationHours: Double = 0.0, // h
)

// ─── Concentration samples ────────────────────────────────────────────────────
data class PreSampleInput(
    val preDoseConcentration: Double = 0.0, // mg/L — measured trough
    // hours before next dose the sample was taken (0 = immediately before dose)
    val hoursBeforeDose: Double = 0.0,
)

data class PostSampleInput(
    val postDoseConcentration: Double = 0.0, // mg/L — measured peak / intermediate
    // hours after END of infusion when sample was drawn
    val hoursAfterEndOfInfusion: Double = 0.0,
)

// ─── Full engine inputs per workflow ─────────────────────────────────────────
data class PreWorkflowInput(
    val patient: PatientInput = PatientInput(),
    val dosing: DosingInput   = DosingInput(),
    val pre: PreSampleInput   = PreSampleInput(),
)

data class PostWorkflowInput(
    val patient: PatientInput = PatientInput(),
    val dosing: DosingInput   = DosingInput(),
    val post: PostSampleInput = PostSampleInput(),
)

data class PrePostWorkflowInput(
    val patient: PatientInput = PatientInput(),
    val dosing: DosingInput   = DosingInput(),
    val pre: PreSampleInput   = PreSampleInput(),
    val post: PostSampleInput = PostSampleInput(),
)
