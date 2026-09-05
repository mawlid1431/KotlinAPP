package com.aiu.tdminsight.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aiu.tdminsight.data.model.*
import com.aiu.tdminsight.ui.LocalUserPrefs
import com.aiu.tdminsight.ui.components.*
import com.aiu.tdminsight.ui.navigation.Routes
import com.aiu.tdminsight.ui.theme.tdm
import com.aiu.tdminsight.ui.theme.TdmNumericLarge
import com.aiu.tdminsight.ui.theme.TdmNumericMedium
import com.aiu.tdminsight.ui.theme.TdmNumericSmall
import com.aiu.tdminsight.ui.theme.TdmNumericMono
import com.aiu.tdminsight.viewmodel.CaseViewModel
import com.aiu.tdminsight.viewmodel.HistoryViewModel

// ==========================================================================
// CaseWizardScreens — The 5-step new-case wizard: patient -> drug -> workflow -> inputs -> review.
// ==========================================================================

// ════════════════════════════════════════════════════════════════════════════
// 04 · NEW CASE — Patient details (Step 1/5)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun NewCaseScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    val crcl = remember(s.patient) {
        runCatching {
            val scrMgDl = s.patient.scrUmolL / 88.4
            val sexFactor = if (s.patient.isMale) 1.0 else 0.85
            if (scrMgDl <= 0 || s.patient.ageLYears <= 0 || s.patient.weightKg <= 0) null
            else ((140.0 - s.patient.ageLYears) * s.patient.weightKg * sexFactor) /
                 (72.0 * scrMgDl)
        }.getOrNull()
    }
    Scaffold(
        topBar = {
            WizardTopBar(
                "Patient details", 1, 5,
                caseId = s.patient.caseId.ifBlank { "Case #001" },
                onBack = { nav.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FictionalDataBanner()

            Text(
                "Enter patient details",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "All data must be fictional for this academic prototype.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            TextField6("Case ID (fictional)", s.patient.caseId,
                onChange = { vm.updatePatient(s.patient.copy(caseId = it)) })

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumField("Weight", s.patient.weightKg, "kg", Modifier.weight(1f), hint = "e.g. 70") {
                    vm.updatePatient(s.patient.copy(weightKg = it)) }
                NumField("Height", s.patient.heightCm, "cm", Modifier.weight(1f), hint = "e.g. 165") {
                    vm.updatePatient(s.patient.copy(heightCm = it)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumField("Age", s.patient.ageLYears.toDouble(), "yr", Modifier.weight(1f), hint = "e.g. 45") {
                    vm.updatePatient(s.patient.copy(ageLYears = it.toInt().coerceAtLeast(0))) }
                SexPicker(s.patient.isMale, Modifier.weight(1f)) {
                    vm.updatePatient(s.patient.copy(isMale = it)) }
            }
            NumField("Serum creatinine", s.patient.scrUmolL, "µmol/L", hint = "e.g. 90") {
                vm.updatePatient(s.patient.copy(scrUmolL = it)) }

            // Live CrCl chip (Fitness-style stat card)
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            Icons.Outlined.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(18.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Cockcroft–Gault CrCl",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (crcl != null) "%.1f mL/min".format(crcl) else "—",
                            style = TdmNumericSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            PrimaryPillButton("Next  →") { nav.navigate(Routes.MEDICATION_SELECT) }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 05 · MEDICATION SELECT  (ZEN. "What brings you to zen?" style)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun MedicationSelectScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    Scaffold(
        topBar = {
            WizardTopBar(
                "Select medication", 2, 5,
                caseId = s.patient.caseId.ifBlank { "Case #001" },
                onBack = { nav.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(22.dp))
            Text(
                "Choose a\nmedication",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Only Vancomycin is modelled in this prototype.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            ZenSelectionCard(
                title = "Vancomycin",
                subtitle = "Pre / Post / Pre+Post sampling",
                selected = true,
                enabled = true,
                icon = Icons.Filled.MedicalServices,
                onClick = { nav.navigate(Routes.WORKFLOW_SELECT) }
            )
            Spacer(Modifier.height(10.dp))
            ZenSelectionCard(
                title = "Gentamicin",
                subtitle = "Coming soon",
                selected = false,
                enabled = false,
                icon = Icons.Outlined.Healing,
                onClick = {}
            )
            Spacer(Modifier.height(10.dp))
            ZenSelectionCard(
                title = "Amikacin",
                subtitle = "Coming soon",
                selected = false,
                enabled = false,
                icon = Icons.Outlined.Science,
                onClick = {}
            )

            Spacer(Modifier.weight(1f))
            PrimaryPillButton("Continue  →") { nav.navigate(Routes.WORKFLOW_SELECT) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 06 · WORKFLOW SELECT  (ZEN. "How experienced are you?" style)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun WorkflowSelectScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    Scaffold(
        topBar = {
            WizardTopBar(
                "Select workflow", 3, 5,
                caseId = s.patient.caseId.ifBlank { "Case #001" },
                onBack = { nav.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(22.dp))
            Text(
                "Pick a\nworkflow",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Choose the sampling strategy you have data for.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            WorkflowCard2("Pre + Post", "Sawchuk–Zaske two-point",
                "Derives individualised Ke and Vd from two concentrations.",
                VancoWorkflow.PRE_POST, vm, nav)
            Spacer(Modifier.height(10.dp))
            WorkflowCard2("Pre only", "Trough + population Ke",
                "Single trough + population Vd + CrCl-estimated clearance.",
                VancoWorkflow.PRE, vm, nav)
            Spacer(Modifier.height(10.dp))
            WorkflowCard2("Post only", "Post-sample + population Vd",
                "Newton-Raphson fit of post-sample with population Vd prior.",
                VancoWorkflow.POST, vm, nav)

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WorkflowCard2(
    title: String, method: String, desc: String,
    w: VancoWorkflow, vm: CaseViewModel, nav: NavController
) {
    val s by vm.uiState.collectAsState()
    val selected = s.selectedWorkflow == w
    Surface(
        onClick = {
            vm.selectWorkflow(w)
            nav.navigate(Routes.INPUT_FORM + "/${w.name.lowercase()}")
        },
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            // Left icon circle
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(method,
                    style = TdmNumericMono.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp))
                Text(desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp))
            }
            Spacer(Modifier.width(10.dp))
            // Radio circle (ZEN. style)
            ZenRadioCircle(selected)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 07–11 · INPUT FORMS (Step 4/5)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun InputFormScreen(nav: NavController, workflow: String, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    LaunchedEffect(workflow, s.patient, s.dosing, s.pre, s.post) { vm.validate() }
    val w = runCatching { VancoWorkflow.valueOf(workflow.uppercase()) }
        .getOrElse { VancoWorkflow.PRE }
    Scaffold(
        topBar = {
            WizardTopBar(
                "Inputs", 4, 5,
                caseId = s.patient.caseId.ifBlank { "Case #001" },
                onBack = { nav.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FictionalDataBanner()

            Text(
                "Enter inputs",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Dosing group
            FormGroup("Dosing") {
                NumField("Dose administered", s.dosing.doseMg, "mg", hint = "e.g. 1000") {
                    vm.updateDosing(s.dosing.copy(doseMg = it)) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumField("Infusion duration", s.dosing.infusionDurationHours, "hours",
                        Modifier.weight(1f), hint = "e.g. 1.0") {
                        vm.updateDosing(s.dosing.copy(infusionDurationHours = it)) }
                    NumField("Dosing interval τ", s.dosing.intervalHours, "hours",
                        Modifier.weight(1f), hint = "e.g. 12") {
                        vm.updateDosing(s.dosing.copy(intervalHours = it)) }
                }
            }

            if (w == VancoWorkflow.PRE || w == VancoWorkflow.PRE_POST) {
                FormGroup("Pre-dose sample (trough)") {
                    NumField("Measured trough concentration",
                        s.pre.preDoseConcentration, "mg/L", hint = "e.g. 10.5") {
                        vm.updatePreSample(s.pre.copy(preDoseConcentration = it)) }
                    NumField("Hours before next dose when sample was taken",
                        s.pre.hoursBeforeDose, "h", hint = "e.g. 11.5") {
                        vm.updatePreSample(s.pre.copy(hoursBeforeDose = it)) }
                }
            }

            if (w == VancoWorkflow.POST || w == VancoWorkflow.PRE_POST) {
                FormGroup("Post-dose sample (peak)") {
                    NumField("Measured peak concentration",
                        s.post.postDoseConcentration, "mg/L", hint = "e.g. 25.0") {
                        vm.updatePostSample(s.post.copy(postDoseConcentration = it)) }
                    NumField("Hours after end of infusion when sample was taken",
                        s.post.hoursAfterEndOfInfusion, "h", hint = "e.g. 2.0") {
                        vm.updatePostSample(s.post.copy(hoursAfterEndOfInfusion = it)) }
                }
            }

            FieldErrorsAndWarnings(s.validationReport)

            // Scan tile (dashed)
            Surface(
                onClick = { },
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DocumentScanner, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Scan a fictional lab report instead",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(4.dp))
            val canContinue = s.validationReport.isValid
            PrimaryPillButton("Review inputs  →", enabled = canContinue) {
                nav.navigate(Routes.REVIEW) }
            if (!canContinue) {
                Text("Fix the errors above to continue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun FormGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            content()
        }
    }
}

@Composable
private fun FieldErrorsAndWarnings(report: com.aiu.tdminsight.data.validation.ValidationReport) {
    if (report.errors.isNotEmpty()) {
        ErrorBanner("Cannot continue",
            report.errors.joinToString(" • ") { it.message })
    }
    if (report.warnings.isNotEmpty()) {
        WarningBanner("Needs review",
            report.warnings.joinToString(" • ") { it.message })
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 12 · REVIEW (Step 5/5)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun ReviewScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    val w = s.selectedWorkflow
    Scaffold(
        topBar = {
            WizardTopBar(
                "Review", 5, 5,
                caseId = s.patient.caseId.ifBlank { "Case #001" },
                onBack = { nav.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FictionalDataBanner()
            Text(
                "Review inputs",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Check everything before calculating.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ReviewGroup("Dosing") {
                ReviewRow("Dose", "${s.dosing.doseMg} mg")
                ReviewRow("Infusion duration", "${s.dosing.infusionDurationHours} h")
                ReviewRow("Dosing interval τ", "${s.dosing.intervalHours} h")
            }
            if (w == VancoWorkflow.PRE || w == VancoWorkflow.PRE_POST) {
                ReviewGroup("Pre-dose sample") {
                    ReviewRow("Concentration", "${s.pre.preDoseConcentration} mg/L")
                    ReviewRow("Sample time", "${s.pre.hoursBeforeDose} h after dose start")
                }
            }
            if (w == VancoWorkflow.POST || w == VancoWorkflow.PRE_POST) {
                ReviewGroup("Post-dose sample") {
                    ReviewRow("Concentration", "${s.post.postDoseConcentration} mg/L")
                    ReviewRow("Sample time", "${s.post.hoursAfterEndOfInfusion} h after dose start")
                }
            }
            Spacer(Modifier.height(8.dp))
            PrimaryPillButton("Run calculation  →") {
                vm.runCalculation()
                nav.navigate(Routes.CALCULATING)
            }
        }
    }
}

@Composable
private fun ReviewGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(0.dp)) {
            Row(
                Modifier.padding(18.dp, 16.dp, 18.dp, 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color = Color.Transparent
                ) {
                    Text(
                        "Edit",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            Column(
                Modifier.padding(horizontal = 18.dp).padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) { content() }
        }
    }
}
