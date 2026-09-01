package com.aiu.tdminsight.domain.engine

import com.aiu.tdminsight.data.model.*
import com.aiu.tdminsight.data.validation.InputValidator
import kotlin.math.exp
import kotlin.math.ln

/**
 * TDM Calculation Engine — pure Kotlin, no Android framework dependencies.
 *
 * CLINICAL SOURCES (must be reviewed/approved by the lecturer before submission):
 *   - Rybak MJ et al., "Therapeutic monitoring of vancomycin for serious
 *     methicillin-resistant Staphylococcus aureus infections",
 *     Am J Health-Syst Pharm, 2020 (AUC₂₄ 400–600 mg·h/L target).
 *   - Malaysian PhIS TDM Calculator (workflow structure: Pre / Post / Pre+Post).
 *   - myTDM Calculator documentation (equation reference).
 *   - Cockcroft DW, Gault MH. Nephron 1976;16(1):31-41 (CrCl).
 *   - Sawchuk-Zaske two-point log-linear regression (Pre+Post).
 *
 * SAMPLE-TIME CONVENTION (per design):
 *   - `tpre`  = hours AFTER dose START that the pre-dose (trough) sample was drawn
 *   - `tpost` = hours AFTER dose START that the post-dose (peak) sample was drawn
 *   Both sit on the same dosing interval [0, τ].
 */
object VancoEngine {

    // ─── AUC₂₄ therapeutic target ──────────────────────────────────────────
    // Source: Rybak 2020 (confirm with lecturer)
    private const val AUC24_TARGET_LOW    = 400.0
    private const val AUC24_TARGET_HIGH   = 600.0
    private const val AUC24_TARGET_CENTRE = 500.0

    // ─── Population PK priors (single-point methods) ─────────────────────
    // Source: standard clinical starting estimates — LECTURER MUST CONFIRM.
    private const val POP_VD_L_PER_KG     = 0.7
    private const val CRCL_TO_CL_FACTOR   = 0.06

    // ─── CrCl via Cockcroft–Gault (mL/min) ────────────────────────────────
    private fun crclMlMin(patient: PatientInput): Double {
        val scrMgDl = patient.scrUmolL / 88.4
        val sexFactor = if (patient.isMale) 1.0 else 0.85
        guardPositive(scrMgDl, "scr (mg/dL)")
        return ((140.0 - patient.ageLYears) * patient.weightKg * sexFactor) /
               (72.0 * scrMgDl)
    }

    /** AUC₂₄ = (Dose/τ) × 24 / CL  */
    private fun auc24FromDoseAndCl(
        doseMg: Double, intervalHours: Double, clLPerHour: Double
    ): Double {
        guardPositive(clLPerHour, "clearance")
        guardPositive(intervalHours, "interval")
        return (doseMg / intervalHours) * (24.0 / clLPerHour)
    }

    /** Recommended per-interval dose to hit target AUC₂₄. */
    private fun recommendedDose(
        targetAuc24: Double, clLPerHour: Double, intervalHours: Double
    ): Double {
        guardPositive(clLPerHour, "clearance")
        guardPositive(intervalHours, "interval")
        return (targetAuc24 * clLPerHour * intervalHours) / 24.0
    }

    // ════════════════════════════════════════════════════════════════════════
    // PRE workflow — trough + population Vd + CrCl→CL
    // ════════════════════════════════════════════════════════════════════════
    fun calculatePre(input: PreWorkflowInput): CalculationResult {
        val report = InputValidator.merge(
            InputValidator.validatePatient(input.patient),
            InputValidator.validateDosing(input.dosing),
            InputValidator.validatePreSample(input.pre, input.dosing.intervalHours),
        )
        if (!report.isValid)
            return CalculationResult.Failure(report.errors.first().message)

        return try {
            val crcl  = crclMlMin(input.patient)
            val vdL   = POP_VD_L_PER_KG * input.patient.weightKg
            val clLH  = crcl * CRCL_TO_CL_FACTOR
            val ke    = clLH / vdL
            guardPositive(ke, "ke")
            val halfLife = ln(2.0) / ke
            val auc24    = auc24FromDoseAndCl(
                input.dosing.doseMg, input.dosing.intervalHours, clLH)
            val recDose  = recommendedDose(
                AUC24_TARGET_CENTRE, clLH, input.dosing.intervalHours)

            CalculationResult.Success(
                workflow = VancoWorkflow.PRE,
                intermediate = PkResults(
                    kePerHour         = ke,
                    halfLifeHours     = halfLife,
                    vdLPerKg          = POP_VD_L_PER_KG,
                    vdL               = vdL,
                    clearanceLPerHour = clLH,
                    auc24             = auc24,
                    recommendedDoseMg = recDose,
                    cmin              = input.pre.preDoseConcentration,
                )
            )
        } catch (e: ArithmeticException) {
            CalculationResult.Failure(e.message ?: "Calculation error.")
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // POST workflow — post-sample + population Vd; Newton-Raphson fit for Ke.
    //
    // Model: one-compartment, constant-rate infusion.
    //   C(t) = (Dose / (k·Vd·T)) · (1 − e^(−kT)) · e^(−k·(t − T))
    // where T = infusion duration, t = time after dose start, t ≥ T.
    //
    // Given observed Cobs at tpost, solve for k by Newton iteration.
    // ════════════════════════════════════════════════════════════════════════
    fun calculatePost(input: PostWorkflowInput): CalculationResult {
        val report = InputValidator.merge(
            InputValidator.validatePatient(input.patient),
            InputValidator.validateDosing(input.dosing),
            InputValidator.validatePostSample(
                input.post, input.dosing.infusionDurationHours),
        )
        if (!report.isValid)
            return CalculationResult.Failure(report.errors.first().message)

        return try {
            val crcl   = crclMlMin(input.patient)
            val vdL    = POP_VD_L_PER_KG * input.patient.weightKg
            val dose   = input.dosing.doseMg
            val tInf   = input.dosing.infusionDurationHours
            val tObs   = input.post.hoursAfterEndOfInfusion
            val cObs   = input.post.postDoseConcentration
            guardPositive(vdL,  "Vd")
            guardPositive(tInf, "infusion duration")
            guardPositive(tObs, "sample time after dose start")

            // f(k) = Cmodel(k) − Cobs
            fun cModel(k: Double): Double {
                if (k <= 1e-9) return 0.0
                return (dose / (k * vdL * tInf)) *
                       (1.0 - exp(-k * tInf)) *
                       exp(-k * (tObs - tInf))
            }
            // Closed-form derivative (used when stable).
            fun cModelDeriv(k: Double): Double {
                if (k <= 1e-9) return 0.0
                val a = dose / (k * vdL * tInf)
                val oneMinusE = 1.0 - exp(-k * tInf)
                val decay = exp(-k * (tObs - tInf))
                val da    = -a / k
                val dOne  = tInf * exp(-k * tInf)
                val dDecay = -(tObs - tInf) * decay
                return (da * oneMinusE + a * dOne) * decay + a * oneMinusE * dDecay
            }

            var ke = 0.3
            var converged = false
            for (i in 0 until 50) {
                val f  = cModel(ke) - cObs
                val fp = cModelDeriv(ke)
                if (!fp.isFinite() || kotlin.math.abs(fp) < 1e-12) break
                val step = f / fp
                val newK = (ke - step).coerceAtLeast(1e-6)
                if (kotlin.math.abs(newK - ke) < 1e-8) { ke = newK; converged = true; break }
                ke = newK
            }
            if (!converged)
                return CalculationResult.Failure(
                    "Newton–Raphson did not converge — check that the post-dose sample time and concentration are physiologically plausible.")
            guardPositive(ke, "ke (post fit)")
            val halfLife = ln(2.0) / ke
            val clLH     = ke * vdL
            val auc24    = auc24FromDoseAndCl(
                input.dosing.doseMg, input.dosing.intervalHours, clLH)
            val recDose  = recommendedDose(
                AUC24_TARGET_CENTRE, clLH, input.dosing.intervalHours)
            // Cmin at end of interval: C(t=τ)
            val cmin = cModel(ke).let {
                (it / exp(-ke * (tObs - tInf))) * exp(-ke * (input.dosing.intervalHours - tInf))
            }
            CalculationResult.Success(
                workflow = VancoWorkflow.POST,
                intermediate = PkResults(
                    kePerHour         = ke,
                    halfLifeHours     = halfLife,
                    vdLPerKg          = POP_VD_L_PER_KG,
                    vdL               = vdL,
                    clearanceLPerHour = clLH,
                    auc24             = auc24,
                    recommendedDoseMg = recDose,
                    cmin              = cmin,
                    cmax              = cObs,
                )
            )
        } catch (e: ArithmeticException) {
            CalculationResult.Failure(e.message ?: "Calculation error.")
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // PRE + POST workflow — Sawchuk-Zaske two-point log-linear regression.
    //   ke = ln(Cpre / Cpost) / (tpre − tpost)
    // where tpre, tpost are both measured from dose start on the same interval,
    // and tpre > tpost (so the denominator is positive).
    // ════════════════════════════════════════════════════════════════════════
    fun calculatePrePost(input: PrePostWorkflowInput): CalculationResult {
        val report = InputValidator.merge(
            InputValidator.validatePatient(input.patient),
            InputValidator.validateDosing(input.dosing),
            InputValidator.validatePreSample(input.pre, input.dosing.intervalHours),
            InputValidator.validatePostSample(input.post, input.dosing.infusionDurationHours),
            InputValidator.validateTimingRelation(
                input.pre.hoursBeforeDose, input.post.hoursAfterEndOfInfusion),
        )
        if (!report.isValid)
            return CalculationResult.Failure(report.errors.first().message)

        val logGuard = InputValidator.guardLogSafety(
            input.pre.preDoseConcentration, input.post.postDoseConcentration)
        if (logGuard is com.aiu.tdminsight.data.validation.FieldResult.Error)
            return CalculationResult.Failure(logGuard.message)

        return try {
            val cPre  = input.pre.preDoseConcentration
            val cPost = input.post.postDoseConcentration
            // dt = tpre − tpost (both in hours after dose start). tpre > tpost ⟹ dt > 0.
            val dt    = input.pre.hoursBeforeDose - input.post.hoursAfterEndOfInfusion
            guardPositive(dt, "Δt between samples")

            // Sawchuk-Zaske ke: concentration falls from cPost (peak) to cPre (trough).
            // ke = ln(Cpeak / Ctrough) / (t_trough − t_peak) — source: Rybak 2020 / myTDM ref.
            guardPositive(cPost, "Cpost")
            guardPositive(cPre,  "Cpre")
            val ke = ln(cPost / cPre) / dt
            guardPositive(ke, "ke")

            val halfLife = ln(2.0) / ke
            val tInf     = input.dosing.infusionDurationHours

            // Vd from one-compartment constant-rate infusion model (Sawchuk-Zaske):
            //   Cmax_eoi = (D / (ke × Vd × T)) × (1 − e^(−ke·T))
            //   → Vd = D × (1 − e^(−ke·T)) / (ke × T × Cmax_eoi)
            // Use cPost as Cmax (assumes peak sample approximates end-of-infusion peak).
            val vdL = input.dosing.doseMg * (1.0 - exp(-ke * tInf)) / (ke * tInf * cPost)
            guardPositive(vdL, "Vd")

            val clLH    = ke * vdL
            val auc24   = auc24FromDoseAndCl(
                input.dosing.doseMg, input.dosing.intervalHours, clLH)
            val recDose = recommendedDose(
                AUC24_TARGET_CENTRE, clLH, input.dosing.intervalHours)

            CalculationResult.Success(
                workflow = VancoWorkflow.PRE_POST,
                intermediate = PkResults(
                    kePerHour         = ke,
                    halfLifeHours     = halfLife,
                    vdL               = vdL,
                    vdLPerKg          = vdL / input.patient.weightKg,
                    clearanceLPerHour = clLH,
                    auc24             = auc24,
                    recommendedDoseMg = recDose,
                    cmin              = cPre,
                    cmax              = cPost,
                )
            )
        } catch (e: ArithmeticException) {
            CalculationResult.Failure(e.message ?: "Calculation error.")
        }
    }

    private fun guardPositive(value: Double, name: String) {
        if (!value.isFinite() || value <= 0.0)
            throw ArithmeticException("Derived $name is non-positive ($value) — check inputs.")
    }
}
