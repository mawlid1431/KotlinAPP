package com.aiu.tdminsight.data.validation

import com.aiu.tdminsight.data.model.*
import org.junit.Assert.*
import org.junit.Test

class InputValidatorTest {

    private fun p(weight: Double = 70.0, age: Int = 55, scr: Double = 88.4,
                 male: Boolean = true) =
        PatientInput(weightKg = weight, ageLYears = age, scrUmolL = scr, isMale = male)

    private fun d(dose: Double = 1000.0, tau: Double = 12.0, tInf: Double = 1.0) =
        DosingInput(doseMg = dose, intervalHours = tau, infusionDurationHours = tInf)

    // ── Patient validation ────────────────────────────────────────────────
    @Test fun `patient zero weight is blocking error`() {
        val r = InputValidator.validatePatient(p(weight = 0.0))
        assertFalse(r.isValid)
        assertTrue(r.errors.any { it.field == "weight" })
    }

    @Test fun `patient extreme weight is blocking error (above 250)`() {
        val r = InputValidator.validatePatient(p(weight = 400.0))
        assertFalse(r.isValid)
    }

    @Test fun `patient high weight in 200 to 250 range is non-blocking warning`() {
        val r = InputValidator.validatePatient(p(weight = 220.0))
        assertTrue(r.isValid)
        assertTrue(r.warnings.any { it.field == "weight" })
    }

    @Test fun `patient zero scr is blocking error`() {
        val r = InputValidator.validatePatient(p(scr = 0.0))
        assertFalse(r.isValid)
        assertTrue(r.errors.any { it.field == "scr" })
    }

    @Test fun `patient age outside range is blocking error`() {
        val r = InputValidator.validatePatient(p(age = 5))
        assertFalse(r.isValid)
    }

    // ── Dosing validation ─────────────────────────────────────────────────
    @Test fun `dosing with infusion longer than interval is blocking error`() {
        val r = InputValidator.validateDosing(d(tau = 4.0, tInf = 5.0))
        assertFalse(r.isValid)
        assertTrue(r.errors.any { it.field == "infusion" })
    }

    @Test fun `dosing with zero dose is blocking error`() {
        val r = InputValidator.validateDosing(d(dose = 0.0))
        assertFalse(r.isValid)
    }

    @Test fun `dosing with very high single dose is non-blocking warning`() {
        val r = InputValidator.validateDosing(d(dose = 2500.0))
        assertTrue(r.isValid)
        assertTrue(r.warnings.any { it.field == "dose" })
    }

    // ── Pre-sample validation ─────────────────────────────────────────────
    @Test fun `pre sample above physiological range is blocking error`() {
        val r = InputValidator.validatePreSample(
            PreSampleInput(preDoseConcentration = 200.0, hoursBeforeDose = 0.0))
        assertFalse(r.isValid)
    }

    @Test fun `pre sample mid-range above 25 is warning only`() {
        val r = InputValidator.validatePreSample(
            PreSampleInput(preDoseConcentration = 40.0, hoursBeforeDose = 0.0))
        assertTrue(r.isValid)
        assertTrue(r.warnings.isNotEmpty())
    }

    @Test fun `pre sample time after interval is blocking error`() {
        val r = InputValidator.validatePreSample(
            PreSampleInput(preDoseConcentration = 10.0, hoursBeforeDose = 14.0),
            intervalHours = 12.0)
        assertFalse(r.isValid)
    }

    // ── Post-sample validation ────────────────────────────────────────────
    @Test fun `post sample drawn during infusion is blocking error`() {
        val r = InputValidator.validatePostSample(
            PostSampleInput(postDoseConcentration = 30.0, hoursAfterEndOfInfusion = 0.5),
            infusionHours = 1.0)
        assertFalse(r.isValid)
        assertTrue(r.errors.any { it.field == "tpost" })
    }

    @Test fun `post sample at zero is blocking error`() {
        val r = InputValidator.validatePostSample(
            PostSampleInput(postDoseConcentration = 0.0, hoursAfterEndOfInfusion = 2.0),
            infusionHours = 1.0)
        assertFalse(r.isValid)
    }

    @Test fun `post sample peak above 60 is non-blocking warning`() {
        val r = InputValidator.validatePostSample(
            PostSampleInput(postDoseConcentration = 70.0, hoursAfterEndOfInfusion = 2.0),
            infusionHours = 1.0)
        assertTrue(r.isValid)
        assertTrue(r.warnings.any { it.field == "cpost" })
    }

    // ── Cross-field timing check ──────────────────────────────────────────
    @Test fun `timing conflict when pre sample is at or before post sample`() {
        val r = InputValidator.validateTimingRelation(
            preHoursBeforeDose = 2.0, postHoursAfterEoi = 5.0)
        assertFalse(r.isValid)
        assertTrue(r.errors.any { it.field == "timing" })
    }

    @Test fun `timing OK when pre sample is after post sample on the same interval`() {
        val r = InputValidator.validateTimingRelation(
            preHoursBeforeDose = 11.5, postHoursAfterEoi = 2.0)
        assertTrue(r.isValid)
        assertTrue(r.warnings.isEmpty())
    }

    // ── Log safety ───────────────────────────────────────────────────────
    @Test fun `log safety rejects negative concentrations`() {
        val r = InputValidator.guardLogSafety(-1.0, 30.0)
        assertTrue(r is FieldResult.Error)
    }

    @Test fun `log safety warns when pre is not less than post`() {
        val r = InputValidator.guardLogSafety(30.0, 8.0)
        assertTrue(r is FieldResult.Warning)
    }

    @Test fun `log safety returns null on valid input`() {
        val r = InputValidator.guardLogSafety(8.0, 30.0)
        assertNull(r)
    }

    // ── Merge ─────────────────────────────────────────────────────────────
    @Test fun `merge aggregates errors and warnings from all reports`() {
        val a = ValidationReport(errors = listOf(FieldResult.Error("x", "x-error")),
                                 warnings = emptyList())
        val b = ValidationReport(errors = emptyList(),
                                 warnings = listOf(FieldResult.Warning("y", "y-warn")))
        val m = InputValidator.merge(a, b)
        assertEquals(1, m.errors.size)
        assertEquals(1, m.warnings.size)
    }
}
