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
// ResultScreens — Calculating, results, engine error and the explanation timeline.
// ==========================================================================

// ════════════════════════════════════════════════════════════════════════════
// 13 · CALCULATING
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun CalculatingScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    LaunchedEffect(s.result) {
        val res = s.result ?: return@LaunchedEffect
        when (res) {
            is CalculationResult.Failure -> nav.navigate(Routes.ERROR) {
                popUpTo(Routes.CALCULATING) { inclusive = true } }
            is CalculationResult.Success -> nav.navigate(Routes.RESULTS) {
                popUpTo(Routes.CALCULATING) { inclusive = true } }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Animated ring (fitness app style loading)
            Box(
                Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 6.dp,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                Icon(
                    Icons.Outlined.Science,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(28.dp))
            Text("Calculating…", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            val method = when (s.selectedWorkflow) {
                VancoWorkflow.PRE      -> "Trough + population Ke"
                VancoWorkflow.POST     -> "Post-sample one-compartment fit"
                VancoWorkflow.PRE_POST -> "Sawchuk–Zaske one-compartment fit"
            }
            Text(method,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            listOf("Validating inputs", "Fitting pharmacokinetic model",
                   "Estimating AUC₂₄", "Generating verdict").forEach { stage ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp),
                        color = MaterialTheme.colorScheme.primary)
                    Text(stage, style = TdmNumericMono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 14 · RESULTS  (Fitness dashboard style with AUC ring)
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    val r = s.result
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Results", style = MaterialTheme.typography.titleMedium)
                        Text(s.patient.caseId.ifBlank { "Case #001" },
                            style = TdmNumericMono,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { (r as? CalculationResult.Success)?.let { shareCase(context, s, it) } },
                        enabled = r is CalculationResult.Success,
                    ) {
                        Icon(Icons.Filled.IosShare, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (r) {
                null -> Text("No result yet.", style = MaterialTheme.typography.bodyMedium)
                is CalculationResult.Failure -> {
                    ErrorBanner("Calculation failed", r.message)
                    Spacer(Modifier.height(8.dp))
                    PrimaryPillButton("Back to inputs") {
                        nav.navigate(Routes.INPUT_FORM +
                            "/${s.selectedWorkflow.name.lowercase()}")
                    }
                }
                is CalculationResult.Success -> ResultBody(r.intermediate, s.dosing.intervalHours, nav)
            }
        }
    }
}

@Composable
private fun ResultBody(pk: PkResults, tau: Double, nav: NavController) {
    val inTarget = (pk.auc24 ?: 0.0) in 400.0..600.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

    // ── AUC ring hero (Fitness calorie-ring style) ─────────────────────────
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ESTIMATED AUC₂₄",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            // Ring + center value
            Box(
                Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = pk.auc24?.let {
                    (it / 600.0).coerceIn(0.0, 1.15)
                }?.toFloat() ?: 0f
                val arcColor = if (inTarget) primaryColor else errorColor

                Canvas(Modifier.fillMaxSize()) {
                    val strokeW = 20.dp.toPx()
                    val radius = size.minDimension / 2f - strokeW / 2f
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    // Track arc
                    drawArc(
                        color = trackColor,
                        startAngle = 135f, sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    // Target zone highlight (400–600 band)
                    val targetStartSweep = 270f * (400f / 600f)
                    drawArc(
                        color = arcColor.copy(alpha = 0.15f),
                        startAngle = 135f + targetStartSweep,
                        sweepAngle = 270f - targetStartSweep,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Butt),
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    // Progress arc
                    if (progress > 0f) {
                        drawArc(
                            color = arcColor,
                            startAngle = 135f,
                            sweepAngle = 270f * progress,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round),
                            topLeft = Offset(cx - radius, cy - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        pk.auc24?.let { "%.0f".format(it) } ?: "—",
                        style = TdmNumericLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "mg·h/L",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Verdict chip
            Surface(
                shape = RoundedCornerShape(50),
                color = if (inTarget) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        if (inTarget) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (inTarget) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        if (inTarget) "In target  ·  400–600 mg·h/L"
                        else "Out of target  ·  consider dose adjustment",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (inTarget) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // 3-stat row (Fitness "Consumed / Burned / Remaining" style)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPill("Ke", pk.kePerHour?.let { "%.4f".format(it) } ?: "—", "h⁻¹")
                StatDivider()
                StatPill("t½", pk.halfLifeHours?.let { "%.1f".format(it) } ?: "—", "h")
                StatDivider()
                StatPill("CL", pk.clearanceLPerHour?.let { "%.2f".format(it) } ?: "—", "L/h")
            }
        }
    }

    // ── Recommendation card ────────────────────────────────────────────────
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "RECOMMENDATION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    pk.recommendedDoseMg?.let { "%.0f".format(it) } ?: "—",
                    style = TdmNumericMedium.copy(fontSize = 28.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "mg every $tau h",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                if (inTarget)
                    "Current regimen is inside the 400–600 mg·h/L target band — confirm against local TDM protocol."
                else
                    "Projection for a single 24-hour interval. Verify with local protocol and clinical judgement.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    // ── PK parameter grid (Fitness metric-card style) ──────────────────────
    Text(
        "PHARMACOKINETIC PARAMETERS",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PkCard("Half-life (t½)", pk.halfLifeHours?.let { "%.1f h".format(it) }, Modifier.weight(1f))
            PkCard("Clearance", pk.clearanceLPerHour?.let { "%.2f L/h".format(it) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PkCard("Vd", pk.vdL?.let { "%.1f L".format(it) }, Modifier.weight(1f))
            PkCard("Vd/kg", pk.vdLPerKg?.let { "%.2f L/kg".format(it) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PkCard("Ke", pk.kePerHour?.let { "%.4f h⁻¹".format(it) }, Modifier.weight(1f))
            PkCard("Cmin", pk.cmin?.let { "%.1f mg/L".format(it) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PkCard("Cmax", pk.cmax?.let { "%.1f mg/L".format(it) }, Modifier.weight(1f))
            PkCard("AUC₂₄", pk.auc24?.let { "%.1f mg·h/L".format(it) }, Modifier.weight(1f))
        }
    }

    // ── Explanation CTA (Fitness "Recent Workouts" list style) ────────────
    Surface(
        onClick = { nav.navigate(Routes.EXPLANATION) },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Outlined.School, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp).size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("How was this calculated?",
                    style = MaterialTheme.typography.titleSmall)
                Text("4 phases · 8 steps with formulas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp))
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(22.dp))
        }
    }

    // ── Disclaimer footer ─────────────────────────────────────────────────
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp, 15.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp))
            Text(
                "Academic result from fictional data. Not a prescribing decision — always confirm with local TDM protocol and clinical judgement.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun StatPill(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = TdmNumericSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(unit, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.width(1.dp).height(40.dp)
        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))
}

@Composable
private fun PkCard(label: String, value: String?, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp, 12.dp)) {
            Text(label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value ?: "—",
                style = TdmNumericSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 6.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 16 · ENGINE ERROR
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun ErrorScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    val r = s.result
    val msg = (r as? CalculationResult.Failure)?.message ?: "An unknown engine error occurred."
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(Icons.Outlined.Report, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(22.dp).size(36.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("This case can't be solved",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text(plainLanguage(msg),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Text("ENGINE · ${technicalLine(msg)}",
                    style = TdmNumericMono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp, 16.dp))
            }
            Spacer(Modifier.height(28.dp))
            PrimaryPillButton("Fix inputs") {
                nav.navigate(Routes.INPUT_FORM +
                    "/${s.selectedWorkflow.name.lowercase()}") {
                    popUpTo(Routes.INPUT_FORM) { inclusive = true }
                }
            }
        }
    }
}

private fun plainLanguage(msg: String): String = when {
    msg.contains("log", ignoreCase = true) ->
        "The two concentrations are equal — the app can't work out how fast the drug is being cleared. Check the pre- and post-dose values."
    msg.contains("timing", ignoreCase = true) ->
        "The pre-dose sample was drawn before (or at the same time as) the post-dose sample."
    msg.contains("non-positive", ignoreCase = true) ->
        "PK parameters came out non-physical for these inputs. Check dose, timing and concentration."
    else -> msg
}

private fun technicalLine(msg: String): String = msg.take(80).replace("\n", " ")

// ════════════════════════════════════════════════════════════════════════════
// 15 · EXPLANATION — 4-phase timeline
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplanationScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    val s by vm.uiState.collectAsState()
    val r = s.result
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How was this calculated?") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (r is CalculationResult.Success) {
                val pk = r.intermediate
                val pt = s.patient; val ds = s.dosing
                val w  = s.selectedWorkflow

                // ── Phase 1: Inputs ──────────────────────────────────────────
                ExplanationPhase(1, "Input values",
                    buildString {
                        append("Patient: ${pt.caseId.ifBlank { "—" }}, ")
                        append("${pt.weightKg} kg, ${pt.ageLYears} yr, ")
                        append("${if (pt.isMale) "Male" else "Female"}, ")
                        append("SCr ${pt.scrUmolL} µmol/L\n")
                        append("Dose: ${ds.doseMg} mg every ${ds.intervalHours} h, ")
                        append("infused over ${ds.infusionDurationHours} h")
                        if (w == VancoWorkflow.PRE || w == VancoWorkflow.PRE_POST) {
                            append("\nPre-dose (trough): ${s.pre.preDoseConcentration} mg/L ")
                            append("at ${s.pre.hoursBeforeDose} h after dose start")
                        }
                        if (w == VancoWorkflow.POST || w == VancoWorkflow.PRE_POST) {
                            append("\nPost-dose (peak): ${s.post.postDoseConcentration} mg/L ")
                            append("at ${s.post.hoursAfterEndOfInfusion} h after dose start")
                        }
                    }
                )

                // ── Phase 2: Method-specific ke derivation ───────────────────
                val phase2Body = when (w) {
                    VancoWorkflow.PRE -> buildString {
                        val scrMgDl = pt.scrUmolL / 88.4
                        val sexF = if (pt.isMale) 1.0 else 0.85
                        val crcl = ((140.0 - pt.ageLYears) * pt.weightKg * sexF) / (72.0 * scrMgDl)
                        val vd   = pk.vdL ?: (0.7 * pt.weightKg)
                        val cl   = pk.clearanceLPerHour ?: 0.0
                        append("Method: Cockcroft–Gault CrCl → population CL → ke\n\n")
                        append("Step 1 — CrCl (Cockcroft–Gault):\n")
                        append("  CrCl = (140 − age) × weight × sex / (72 × SCr_mg/dL)\n")
                        append("       = (140 − ${pt.ageLYears}) × ${pt.weightKg} × $sexF\n")
                        append("         / (72 × ${"%.3f".format(scrMgDl)})\n")
                        append("       = ${"%.1f".format(crcl)} mL/min\n\n")
                        append("Step 2 — Population Vd (0.7 L/kg):\n")
                        append("  Vd = 0.7 × ${pt.weightKg} = ${"%.1f".format(vd)} L\n\n")
                        append("Step 3 — Clearance (CL = CrCl × 0.06):\n")
                        append("  CL = ${"%.1f".format(crcl)} × 0.06 = ${"%.2f".format(cl)} L/h\n\n")
                        append("Step 4 — Ke:\n")
                        append("  ke = CL / Vd = ${"%.2f".format(cl)} / ${"%.1f".format(vd)}")
                        pk.kePerHour?.let { append(" = ${"%.4f".format(it)} h⁻¹") }
                    }
                    VancoWorkflow.POST -> buildString {
                        append("Method: One-compartment Newton-Raphson fit (post-dose sample)\n\n")
                        append("Model: C(t) = (D / (ke·Vd·T)) × (1 − e^(−ke·T)) × e^(−ke·(t−T))\n")
                        append("  D = ${ds.doseMg} mg, T = ${ds.infusionDurationHours} h\n")
                        append("  Population Vd = 0.7 × ${pt.weightKg} = ${"%.1f".format(0.7 * pt.weightKg)} L\n\n")
                        append("Step 1 — Solve for ke via Newton-Raphson iteration\n")
                        append("  (50 iterations starting at ke₀ = 0.3 h⁻¹)\n")
                        append("  C_observed = ${s.post.postDoseConcentration} mg/L ")
                        append("at t = ${s.post.hoursAfterEndOfInfusion} h\n\n")
                        append("Step 2 — Derived ke:")
                        pk.kePerHour?.let { append(" ${"%.4f".format(it)} h⁻¹") }
                        pk.halfLifeHours?.let { append("  (t½ = ${"%.1f".format(it)} h)") }
                    }
                    VancoWorkflow.PRE_POST -> buildString {
                        val dt = s.pre.hoursBeforeDose - s.post.hoursAfterEndOfInfusion
                        append("Method: Sawchuk–Zaske two-point log-linear regression\n\n")
                        append("Step 1 — Elimination rate constant (ke):\n")
                        append("  ke = ln(C_peak / C_trough) / (t_trough − t_peak)\n")
                        append("     = ln(${s.post.postDoseConcentration} / ${s.pre.preDoseConcentration})")
                        append(" / (${s.pre.hoursBeforeDose} − ${s.post.hoursAfterEndOfInfusion})\n")
                        append("     = ln(${"%.3f".format(s.post.postDoseConcentration / s.pre.preDoseConcentration)})")
                        append(" / ${"%.1f".format(dt)} h")
                        pk.kePerHour?.let { append(" = ${"%.4f".format(it)} h⁻¹") }
                        append("\n\nStep 2 — Half-life:\n")
                        append("  t½ = ln(2) / ke")
                        pk.halfLifeHours?.let { append(" = ${"%.2f".format(it)} h") }
                        append("\n\nStep 3 — Volume of distribution (Vd):\n")
                        append("  Vd = D × (1 − e^(−ke·T)) / (ke × T × C_peak)\n")
                        pk.vdL?.let { append("     = ${"%.1f".format(it)} L") }
                        pk.vdLPerKg?.let { append("  (${"%.2f".format(it)} L/kg)") }
                    }
                }
                ExplanationPhase(2, "Ke derivation — ${
                    when(w) {
                        VancoWorkflow.PRE      -> "Cockcroft–Gault method"
                        VancoWorkflow.POST     -> "Newton-Raphson fit"
                        VancoWorkflow.PRE_POST -> "Sawchuk–Zaske method"
                    }
                }", phase2Body)

                // ── Phase 3: PK parameters ───────────────────────────────────
                ExplanationPhase(3, "Pharmacokinetic parameters",
                    buildString {
                        pk.kePerHour?.let           { append("ke             = ${"%.4f".format(it)} h⁻¹\n") }
                        pk.halfLifeHours?.let        { append("t½             = ${"%.2f".format(it)} h\n") }
                        pk.vdL?.let                  { append("Vd             = ${"%.1f".format(it)} L\n") }
                        pk.vdLPerKg?.let             { append("Vd/kg          = ${"%.2f".format(it)} L/kg\n") }
                        pk.clearanceLPerHour?.let    { append("Clearance (CL) = ${"%.2f".format(it)} L/h\n") }
                        pk.cmin?.let                 { append("C_min (trough) = ${"%.1f".format(it)} mg/L\n") }
                        pk.cmax?.let                 { append("C_max (peak)   = ${"%.1f".format(it)} mg/L\n") }
                        append("\nAUC₂₄ formula:\n")
                        append("  AUC₂₄ = (Dose / τ) × 24 / CL")
                        pk.clearanceLPerHour?.let { cl ->
                            append("\n       = (${ds.doseMg} / ${ds.intervalHours}) × 24 / ${"%.2f".format(cl)}")
                        }
                    }
                )

                // ── Phase 4: Final result ────────────────────────────────────
                ExplanationPhase(4, "Final result",
                    buildString {
                        append("Estimated AUC₂₄ = ")
                        append(pk.auc24?.let { "${"%.1f".format(it)} mg·h/L" } ?: "—")
                        append("\nTarget range: 400–600 mg·h/L (Rybak 2020)\n\n")
                        append("Recommended dose:\n")
                        append("  Dose = AUC_target × CL × τ / 24\n")
                        append("       = 500 × ${pk.clearanceLPerHour?.let { "%.2f".format(it) } ?: "?"} × ${ds.intervalHours} / 24\n")
                        append("       = ")
                        append(pk.recommendedDoseMg?.let { "${"%.0f".format(it)} mg every ${ds.intervalHours} h" } ?: "—")
                    }
                )
            } else {
                Text("No result available.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ExplanationPhase(n: Int, title: String, body: String) {
    Row(
        Modifier.padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Text(
                "$n",
                color = MaterialTheme.colorScheme.onPrimary,
                style = TdmNumericMono.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(8.dp)
            )
        }
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/**
 * Builds a plain-text summary of the case currently on screen and hands it to
 * Android's share sheet. The text is generated from the passed-in state, so it
 * always describes this case and never a previously viewed one.
 */
private fun shareCase(
    context: android.content.Context,
    state: com.aiu.tdminsight.viewmodel.CaseUiState,
    result: CalculationResult.Success,
) {
    val pk = result.intermediate
    val p  = state.patient
    val d  = state.dosing
    val label = p.caseId.ifBlank { "Untitled case" }

    val body = buildString {
        appendLine("TDM Insight - $label")
        appendLine("Workflow: ${result.workflow.name}")
        appendLine()
        appendLine("PATIENT")
        appendLine("  Weight: ${p.weightKg} kg")
        appendLine("  Age: ${p.ageLYears} years")
        appendLine("  Sex: ${if (p.isMale) "Male" else "Female"}")
        appendLine("  Serum creatinine: ${p.scrUmolL} umol/L")
        appendLine()
        appendLine("REGIMEN")
        appendLine("  Dose: ${d.doseMg} mg every ${d.intervalHours} h")
        appendLine("  Infusion duration: ${d.infusionDurationHours} h")
        appendLine()
        appendLine("RESULTS")
        appendLine("  AUC24: ${"%.1f".format(pk.auc24)} mg.h/L")
        appendLine("  Recommended dose: ${"%.0f".format(pk.recommendedDoseMg)} mg")
        appendLine("  Ke: ${"%.4f".format(pk.kePerHour)} /h")
        appendLine("  Half-life: ${"%.1f".format(pk.halfLifeHours)} h")
        appendLine("  Vd: ${"%.1f".format(pk.vdL)} L (${"%.2f".format(pk.vdLPerKg)} L/kg)")
        appendLine("  Clearance: ${"%.2f".format(pk.clearanceLPerHour)} L/h")
        appendLine()
        appendLine("TDM Insight is an academic prototype (CDE2313, AIU).")
        append("This case is fictional. Never use for real prescribing decisions.")
    }

    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, "TDM Insight - $label")
        putExtra(android.content.Intent.EXTRA_TEXT, body)
    }
    context.startActivity(android.content.Intent.createChooser(send, "Share case"))
}
