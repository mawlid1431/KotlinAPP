package com.aiu.tdminsight.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiu.tdminsight.TdmApplication
import com.aiu.tdminsight.data.model.*
import com.aiu.tdminsight.data.supabase.SupabaseRepository
import com.aiu.tdminsight.data.validation.InputValidator
import com.aiu.tdminsight.data.validation.ValidationReport
import com.aiu.tdminsight.domain.engine.VancoEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

data class CaseUiState(
    val patient: PatientInput        = PatientInput(
        caseId = "", weightKg = 0.0, heightCm = 0.0,
        ageLYears = 0, isMale = true, scrUmolL = 0.0,
    ),
    val dosing: DosingInput          = DosingInput(
        doseMg = 0.0, intervalHours = 0.0, infusionDurationHours = 0.0,
    ),
    val pre: PreSampleInput          = PreSampleInput(
        preDoseConcentration = 0.0, hoursBeforeDose = 0.0,
    ),
    val post: PostSampleInput        = PostSampleInput(
        postDoseConcentration = 0.0, hoursAfterEndOfInfusion = 0.0,
    ),
    val selectedWorkflow: VancoWorkflow = VancoWorkflow.PRE_POST,
    val validationReport: ValidationReport = ValidationReport(),
    val result: CalculationResult?   = null,
    val isCalculating: Boolean       = false,
)

/**
 * CaseViewModel — coordinates the TDM wizard state.
 *
 * Changed from ViewModel to AndroidViewModel so it can access the
 * Application-scoped SupabaseRepository singleton without a DI framework.
 * The change is transparent to callers: viewModel() still works.
 */
class CaseViewModel(application: Application) : AndroidViewModel(application) {

    private val supabaseRepository: SupabaseRepository =
        (application as TdmApplication).supabaseRepository

    private val _uiState = MutableStateFlow(CaseUiState())
    val uiState: StateFlow<CaseUiState> = _uiState.asStateFlow()

    fun updatePatient(patient: PatientInput) {
        _uiState.update { it.copy(patient = patient) }
    }

    fun updateDosing(dosing: DosingInput) {
        _uiState.update { it.copy(dosing = dosing) }
    }

    fun updatePreSample(pre: PreSampleInput) {
        _uiState.update { it.copy(pre = pre) }
    }

    fun updatePostSample(post: PostSampleInput) {
        _uiState.update { it.copy(post = post) }
    }

    fun selectWorkflow(workflow: VancoWorkflow) {
        _uiState.update { it.copy(selectedWorkflow = workflow) }
    }

    fun validate() {
        val s = _uiState.value
        val report = when (s.selectedWorkflow) {
            VancoWorkflow.PRE -> InputValidator.merge(
                InputValidator.validatePatient(s.patient),
                InputValidator.validateDosing(s.dosing),
                InputValidator.validatePreSample(s.pre, s.dosing.intervalHours),
            )
            VancoWorkflow.POST -> InputValidator.merge(
                InputValidator.validatePatient(s.patient),
                InputValidator.validateDosing(s.dosing),
                InputValidator.validatePostSample(s.post, s.dosing.infusionDurationHours),
            )
            VancoWorkflow.PRE_POST -> InputValidator.merge(
                InputValidator.validatePatient(s.patient),
                InputValidator.validateDosing(s.dosing),
                InputValidator.validatePreSample(s.pre, s.dosing.intervalHours),
                InputValidator.validatePostSample(s.post, s.dosing.infusionDurationHours),
                InputValidator.validateTimingRelation(
                    s.pre.hoursBeforeDose, s.post.hoursAfterEndOfInfusion),
            )
        }
        _uiState.update { it.copy(validationReport = report) }
    }

    fun runCalculation() {
        validate()
        // Read the snapshot AFTER validating so the inputs and the validation
        // verdict below are guaranteed to describe the same state.
        val s = _uiState.value
        if (!s.validationReport.isValid) return

        _uiState.update { it.copy(isCalculating = true, result = null) }
        viewModelScope.launch {
            // The PK engine is pure CPU work — keep it off the main thread.
            val result = withContext(Dispatchers.Default) {
                when (s.selectedWorkflow) {
                    VancoWorkflow.PRE ->
                        VancoEngine.calculatePre(PreWorkflowInput(s.patient, s.dosing, s.pre))
                    VancoWorkflow.POST ->
                        VancoEngine.calculatePost(PostWorkflowInput(s.patient, s.dosing, s.post))
                    VancoWorkflow.PRE_POST ->
                        VancoEngine.calculatePrePost(PrePostWorkflowInput(s.patient, s.dosing, s.pre, s.post))
                }
            }
            _uiState.update { it.copy(result = result, isCalculating = false) }

            // Fire-and-forget persistence — a Supabase failure never affects the result shown to the user.
            if (result is CalculationResult.Success) {
                launch {
                    try {
                        supabaseRepository.saveCase(s.patient, s.dosing, s.pre, s.post, result)
                    } catch (e: Exception) {
                        Log.w("CaseViewModel", "Supabase save skipped: ${e.message}")
                    }
                }
            }
        }
    }
}
