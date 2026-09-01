package com.aiu.tdminsight.data.validation

import com.aiu.tdminsight.data.model.*

// Pure-Kotlin validator — no Android framework imports.
// Returns a ValidationReport listing every blocking error and every advisory warning.
//
// Per the design reference (design/TDM Insight.dc.html), the form exposes:
//   - dose (mg):           min 100, max 4000, warn if > 2000
//   - infusion (h):        min 0.25, max 6
//   - interval τ (h):      min 4, max 72
//   - post-dose time (h after dose start): min 0, max 72
//   - post-dose conc (mg/L):  min 0.1, max 120, warn if > 60
//   - pre-dose time (h after dose start):  min 0, max 72
//   - pre-dose conc (mg/L):  min 0.1, max 80, warn if > 25
//
// The cross-field "Timing conflict" banner (screen 11) fires when:
//   1. tpost < tinf  (sample taken during the infusion)
//   2. tpre  > τ     (sample taken after the next dose)
//   3. workflow is Pre+Post AND tpre ≤ tpost (samples in wrong order on the interval)
object InputValidator {

    // ── Patient validation ────────────────────────────────────────────────
    fun validatePatient(p: PatientInput): ValidationReport {
        val errors = mutableListOf<FieldResult.Error>()
        val warnings = mutableListOf<FieldResult.Warning>()

        if (p.weightKg < 20 || p.weightKg > 250)
            errors += FieldResult.Error("weight", "Weight outside physiological range (20–250 kg).")
        else if (p.weightKg > 200)
            warnings += FieldResult.Warning("weight", "Weight > 200 kg — please review.")

        if (p.ageLYears < 16 || p.ageLYears > 110)
            errors += FieldResult.Error("age", "Age outside physiological range (16–110 years).")

        if (p.scrUmolL < 20 || p.scrUmolL > 1200)
            errors += FieldResult.Error("scr", "SCr outside physiological range (20–1200 µmol/L).")
        else if (p.scrUmolL > 200)
            warnings += FieldResult.Warning("scr", "Marked renal impairment — double-check the value.")

        return ValidationReport(errors, warnings)
    }

    // ── Dosing validation ─────────────────────────────────────────────────
    fun validateDosing(d: DosingInput): ValidationReport {
        val errors = mutableListOf<FieldResult.Error>()
        val warnings = mutableListOf<FieldResult.Warning>()

        if (d.doseMg < 100 || d.doseMg > 4000)
            errors += FieldResult.Error("dose", "Dose outside physiological range (100–4000 mg).")
        else if (d.doseMg > 2000)
            warnings += FieldResult.Warning("dose", "Unusually high single dose — please double-check.")

        if (d.intervalHours < 4 || d.intervalHours > 72)
            errors += FieldResult.Error("interval", "Interval outside physiological range (4–72 h).")

        if (d.infusionDurationHours < 0.25 || d.infusionDurationHours > 6)
            errors += FieldResult.Error("infusion", "Infusion duration outside range (0.25–6 h).")

        if (d.intervalHours > 0 && d.infusionDurationHours >= d.intervalHours)
            errors += FieldResult.Error("infusion", "Infusion duration must be shorter than the dosing interval.")

        return ValidationReport(errors, warnings)
    }

    // ── Pre-sample validation ─────────────────────────────────────────────
    fun validatePreSample(pre: PreSampleInput, intervalHours: Double = 0.0): ValidationReport {
        val errors = mutableListOf<FieldResult.Error>()
        val warnings = mutableListOf<FieldResult.Warning>()

        if (pre.preDoseConcentration < 0.1 || pre.preDoseConcentration > 80)
            errors += FieldResult.Error("cpre",
                "Outside physiological range (0.1–80 mg/L).")
        else if (pre.preDoseConcentration > 25)
            warnings += FieldResult.Warning("cpre",
                "Trough above the usual 10–20 mg/L range.")

        if (pre.hoursBeforeDose < 0 || pre.hoursBeforeDose > 72)
            errors += FieldResult.Error("tpre",
                "Pre-dose sample time outside range (0–72 h after dose start).")

        if (intervalHours > 0 && pre.hoursBeforeDose > intervalHours)
            errors += FieldResult.Error("tpre",
                "Pre-dose sample time falls after the next dose is due. It must sit within the interval τ.")

        return ValidationReport(errors, warnings)
    }

    // ── Post-sample validation ────────────────────────────────────────────
    fun validatePostSample(post: PostSampleInput,
                          infusionHours: Double = 0.0): ValidationReport {
        val errors = mutableListOf<FieldResult.Error>()
        val warnings = mutableListOf<FieldResult.Warning>()

        if (post.postDoseConcentration < 0.1 || post.postDoseConcentration > 120)
            errors += FieldResult.Error("cpost",
                "Outside physiological range (0.1–120 mg/L).")
        else if (post.postDoseConcentration > 60)
            warnings += FieldResult.Warning("cpost",
                "Very high peak — verify the assay result.")

        if (post.hoursAfterEndOfInfusion < 0 || post.hoursAfterEndOfInfusion > 72)
            errors += FieldResult.Error("tpost",
                "Post-dose sample time outside range (0–72 h after dose start).")

        // tpost is "hours after dose start" (per design RULES.tpost.unit)
        if (infusionHours > 0 && post.hoursAfterEndOfInfusion < infusionHours)
            errors += FieldResult.Error("tpost",
                "Post-dose sample time is inside the infusion. The sample must be taken after the infusion ends (≥ infusion duration).")

        return ValidationReport(errors, warnings)
    }

    /**
     * Cross-field timing check (used by the Pre+Post workflow).
     * Per the design, when both samples are on the same dosing interval,
     * the pre-dose sample must be DRAWN LATER than the post-dose sample.
     */
    fun validateTimingRelation(
        preHoursBeforeDose: Double,
        postHoursAfterEoi: Double,
    ): ValidationReport {
        val errors = mutableListOf<FieldResult.Error>()
        val warnings = mutableListOf<FieldResult.Warning>()

        if (preHoursBeforeDose <= postHoursAfterEoi) {
            errors += FieldResult.Error("timing",
                "Pre-dose sample must be drawn later in the interval than the post-dose sample. Check both sampling times.")
        }
        return ValidationReport(errors, warnings)
    }

    /** Math-safety guard — prevents log(0) / log(negative) inside the engine.
     *
     * Normal: pre (trough) < post (peak) → ln(post/pre) > 0 → ke > 0.
     * Abnormal: pre ≥ post (trough ≥ peak) → ke ≤ 0, which is non-physical.
     */
    fun guardLogSafety(pre: Double, post: Double): FieldResult? {
        if (pre <= 0 || post <= 0) return FieldResult.Error("concentration",
            "Concentrations must be positive for logarithmic calculations.")
        if (pre >= post) return FieldResult.Warning("concentration",
            "Pre-dose concentration is not less than post-dose — result may be unreliable.")
        return null
    }

    fun merge(vararg reports: ValidationReport): ValidationReport = ValidationReport(
        errors   = reports.flatMap { it.errors },
        warnings = reports.flatMap { it.warnings },
    )
}
