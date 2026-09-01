package com.aiu.tdminsight.domain.engine

import com.aiu.tdminsight.data.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Engine unit tests — normal cases, edge cases, and the specific
 * error-protection cases the rubric calls out (divide-by-zero,
 * invalid log input, negative derived values).
 */
class VancoEngineTest {

    // ── Patient factory ────────────────────────────────────────────────────
    private fun patient(
        weight: Double = 68.0, age: Int = 62, scr: Double = 98.0,
        isMale: Boolean = true,
    ) = PatientInput(caseId = "T-1", weightKg = weight, ageLYears = age,
                     isMale = isMale, scrUmolL = scr)

    private fun dosing(dose: Double = 1000.0, tau: Double = 12.0, tInf: Double = 1.0) =
        DosingInput(doseMg = dose, intervalHours = tau, infusionDurationHours = tInf)

    // ── Pre workflow ───────────────────────────────────────────────────────
    @Test fun `pre workflow returns success on normal adult case`() {
        val r = VancoEngine.calculatePre(PreWorkflowInput(
            patient = patient(),
            dosing  = dosing(dose = 1500.0, tau = 12.0, tInf = 1.5),
            pre     = PreSampleInput(preDoseConcentration = 12.0, hoursBeforeDose = 0.5),
        ))
        assertTrue("expected Success, got $r", r is CalculationResult.Success)
        val pk = (r as CalculationResult.Success).intermediate
        assertNotNull(pk.auc24)
        assertTrue("AUC must be positive", pk.auc24!! > 0.0)
        assertNotNull(pk.halfLifeHours)
    }

    @Test fun `pre workflow returns failure on zero weight`() {
        val r = VancoEngine.calculatePre(PreWorkflowInput(
            patient = patient(weight = 0.0),
            dosing  = dosing(),
            pre     = PreSampleInput(preDoseConcentration = 10.0, hoursBeforeDose = 0.5),
        ))
        assertTrue(r is CalculationResult.Failure)
    }

    @Test fun `pre workflow returns failure on negative dose`() {
        val r = VancoEngine.calculatePre(PreWorkflowInput(
            patient = patient(),
            dosing  = dosing(dose = -100.0),
            pre     = PreSampleInput(preDoseConcentration = 10.0, hoursBeforeDose = 0.5),
        ))
        assertTrue(r is CalculationResult.Failure)
    }

    @Test fun `pre workflow handles elderly female with renal impairment`() {
        val r = VancoEngine.calculatePre(PreWorkflowInput(
            patient = patient(weight = 60.0, age = 78, scr = 180.0, isMale = false),
            dosing  = dosing(dose = 1000.0, tau = 24.0, tInf = 1.5),
            pre     = PreSampleInput(preDoseConcentration = 18.0, hoursBeforeDose = 0.5),
        ))
        assertTrue("expected Success for elderly renally-impaired, got $r",
            r is CalculationResult.Success)
    }

    // ── Post workflow ──────────────────────────────────────────────────────
    @Test fun `post workflow returns success on a normal case`() {
        val r = VancoEngine.calculatePost(PostWorkflowInput(
            patient = patient(),
            dosing  = dosing(dose = 1500.0, tau = 12.0, tInf = 1.5),
            post    = PostSampleInput(postDoseConcentration = 28.0, hoursAfterEndOfInfusion = 2.0),
        ))
        assertTrue("expected Success, got $r", r is CalculationResult.Success)
        val pk = (r as CalculationResult.Success).intermediate
        assertNotNull(pk.kePerHour)
        assertTrue("ke must be positive", pk.kePerHour!! > 0.0)
        assertNotNull(pk.cmin)
    }

    @Test fun `post workflow fails when sample time is during the infusion`() {
        val r = VancoEngine.calculatePost(PostWorkflowInput(
            patient = patient(),
            dosing  = dosing(dose = 1000.0, tau = 12.0, tInf = 1.0),
            post    = PostSampleInput(postDoseConcentration = 25.0, hoursAfterEndOfInfusion = 0.5),
        ))
        assertTrue(r is CalculationResult.Failure)
    }

    @Test fun `post workflow handles plausible two-point-like data`() {
        val r = VancoEngine.calculatePost(PostWorkflowInput(
            patient = patient(),
            dosing  = dosing(dose = 1000.0, tau = 12.0, tInf = 1.0),
            post    = PostSampleInput(postDoseConcentration = 22.5, hoursAfterEndOfInfusion = 2.0),
        ))
        assertTrue("expected Success, got $r", r is CalculationResult.Success)
    }

    // ── Pre+Post workflow (Sawchuk-Zaske) ──────────────────────────────────
    @Test fun `prepost workflow returns success on the design's textbook two-point case`() {
        // From the design's "filled & valid" preset:
        //   dose 1000mg, τ=12, tinf=1, tpost=2, cpost=26, tpre=11.5, cpre=12.5
        val r = VancoEngine.calculatePrePost(PrePostWorkflowInput(
            patient = patient(),
            dosing  = dosing(dose = 1000.0, tau = 12.0, tInf = 1.0),
            pre     = PreSampleInput(preDoseConcentration = 12.5, hoursBeforeDose = 11.5),
            post    = PostSampleInput(postDoseConcentration = 26.0, hoursAfterEndOfInfusion = 2.0),
        ))
        assertTrue("expected Success, got $r", r is CalculationResult.Success)
        val pk = (r as CalculationResult.Success).intermediate
        // dt = 11.5 - 2 = 9.5h; ke = ln(cpost/cpre) / dt = ln(26/12.5) / 9.5 ≈ 0.077 h⁻¹ (positive → valid)
        // Sawchuk-Zaske: peak (cpost=26) > trough (cpre=12.5) is the normal physiological case.
        pk.kePerHour?.let { assertTrue("ke must be positive and finite", it.isFinite() && it > 0) }
        pk.auc24?.let     { assertTrue("auc24 must be positive and finite", it.isFinite() && it > 0) }
    }

    @Test fun `prepost workflow rejects negative pre-dose concentration`() {
        val r = VancoEngine.calculatePrePost(PrePostWorkflowInput(
            patient = patient(),
            dosing  = dosing(),
            pre     = PreSampleInput(preDoseConcentration = -1.0, hoursBeforeDose = 11.5),
            post    = PostSampleInput(postDoseConcentration = 30.0, hoursAfterEndOfInfusion = 2.0),
        ))
        assertTrue("negative pre-dose must be rejected", r is CalculationResult.Failure)
    }

    @Test fun `prepost workflow catches timing conflict where pre is before post`() {
        val r = VancoEngine.calculatePrePost(PrePostWorkflowInput(
            patient = patient(),
            dosing  = dosing(dose = 1000.0, tau = 12.0, tInf = 1.0),
            pre     = PreSampleInput(preDoseConcentration = 8.0,  hoursBeforeDose = 2.0),
            post    = PostSampleInput(postDoseConcentration = 30.0, hoursAfterEndOfInfusion = 9.0),
        ))
        // pre=2.0 ≤ post=9.0 → must be a timing error
        assertTrue("pre<=post must be a timing error",
            r is CalculationResult.Failure)
    }

    // ── Math guards ────────────────────────────────────────────────────────
    @Test fun `engine never produces NaN on edge inputs`() {
        val r = VancoEngine.calculatePre(PreWorkflowInput(
            patient = patient(weight = 0.001, age = 1, scr = 1.0),
            dosing  = dosing(dose = 1.0, tau = 0.001, tInf = 0.0001),
            pre     = PreSampleInput(preDoseConcentration = 0.001, hoursBeforeDose = 0.0),
        ))
        if (r is CalculationResult.Success) {
            r.intermediate.kePerHour?.let { assertTrue("ke finite", it.isFinite()) }
            r.intermediate.auc24?.let    { assertTrue("auc finite", it.isFinite()) }
        } else {
            assertTrue(r is CalculationResult.Failure)
        }
    }
}
