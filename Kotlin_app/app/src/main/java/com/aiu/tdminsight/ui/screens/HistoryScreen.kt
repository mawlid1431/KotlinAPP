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
// HistoryScreen — Saved cases from Supabase, with the PK curve graph.
// ==========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    nav: NavController,
    vm: HistoryViewModel = viewModel(),
) {
    val entries   by vm.entries.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val isLive    by vm.isLiveData.collectAsState()

    // Re-query on every entry. The ViewModel only loaded in its init{}, which
    // runs once at sign-in - before any case has been saved - so a case saved
    // later never showed up until the app was restarted.
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
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
        bottomBar = { BottomNavBar(current = Routes.HISTORY) { nav.navigate(it) } },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Calculation History",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                if (isLive) "Your saved calculations" else "No calculations yet — complete a case to see your history",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(20.dp))

            when {
                isLoading -> Box(
                    Modifier.fillMaxWidth().padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
                entries.isEmpty() -> RecentCasesEmpty()
                else -> entries.forEach { entry ->
                    HistoryCaseCard(entry)
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HistoryCaseCard(e: HistoryEntry) {
    val tdm = MaterialTheme.tdm
    val inTarget = e.auc24 in 400.0..600.0
    val statusColor = when {
        inTarget         -> tdm.success
        e.auc24 > 600.0  -> MaterialTheme.colorScheme.error
        else             -> tdm.warning
    }
    val statusLabel = when {
        inTarget         -> "In target"
        e.auc24 > 600.0  -> "Above target"
        else             -> "Below target"
    }
    val workflowLabel = when (e.workflow) {
        VancoWorkflow.PRE      -> "Pre (trough)"
        VancoWorkflow.POST     -> "Post (peak)"
        VancoWorkflow.PRE_POST -> "Pre + Post"
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ── Header row ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(e.caseId,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(e.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Workflow badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(workflowLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            // ── AUC₂₄ result row ──────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text("AUC₂₄",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.0f".format(e.auc24)} mg·h/L",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = statusColor)
                    Text(statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor)
                }
                Column {
                    Text("Rec. dose",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.0f".format(e.recDoseMg)} mg",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("every ${e.intervalH.toInt()} h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── PK parameters row ─────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PkStatChip("ke", "${"%.3f".format(e.ke)} h⁻¹", Modifier.weight(1f))
                PkStatChip("t½", "${"%.1f".format(e.t12)} h",  Modifier.weight(1f))
                PkStatChip("Vd", "${"%.0f".format(e.vdL)} L",  Modifier.weight(1f))
                PkStatChip("CL", "${"%.2f".format(e.clLH)} L/h", Modifier.weight(1f))
            }

            // ── Concentration–time graph ──────────────────────────────────
            PkCurveGraph(
                ke = e.ke, vdL = e.vdL, doseMg = e.doseMg,
                intervalH = e.intervalH, tInfH = e.tInfH,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun PkStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PkCurveGraph(
    ke: Double, vdL: Double, doseMg: Double,
    intervalH: Double, tInfH: Double,
    modifier: Modifier = Modifier,
) {
    val primary   = MaterialTheme.colorScheme.primary
    val surface   = MaterialTheme.colorScheme.surfaceContainerHigh
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    // pre-compute curve points over [0, τ]
    val steps = 120
    val points = remember(ke, vdL, doseMg, intervalH, tInfH) {
        (0..steps).map { i ->
            val t = intervalH * i / steps
            val c = if (t <= tInfH) {
                if (ke < 1e-9) 0.0
                else (doseMg / (ke * vdL * tInfH)) * (1.0 - kotlin.math.exp(-ke * t))
            } else {
                val cEoi = (doseMg / (ke * vdL * tInfH)) * (1.0 - kotlin.math.exp(-ke * tInfH))
                cEoi * kotlin.math.exp(-ke * (t - tInfH))
            }
            Pair(t.toFloat(), c.toFloat())
        }
    }
    val cMax = points.maxOf { it.second }.coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val padL = 8f; val padR = 8f; val padT = 8f; val padB = 20f
        val plotW = w - padL - padR
        val plotH = h - padT - padB

        // Grid lines (y = 25%, 50%, 75%)
        listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
            val y = padT + plotH * (1f - frac)
            drawLine(gridColor, Offset(padL, y), Offset(w - padR, y), strokeWidth = 1f)
        }

        // Infusion end vertical marker
        val tInfX = padL + plotW * (tInfH / intervalH).toFloat()
        drawLine(gridColor, Offset(tInfX, padT), Offset(tInfX, padT + plotH),
            strokeWidth = 1.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))

        // Curve fill (background area under curve)
        val fillPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(padL + plotW * (points.first().first / intervalH.toFloat()), padT + plotH)
            points.forEach { (t, c) ->
                lineTo(padL + plotW * (t / intervalH.toFloat()), padT + plotH * (1f - c / cMax))
            }
            lineTo(padL + plotW * (points.last().first / intervalH.toFloat()), padT + plotH)
            close()
        }
        drawPath(fillPath, color = primary.copy(alpha = 0.08f))

        // Curve line
        val linePath = androidx.compose.ui.graphics.Path().apply {
            val first = points.first()
            moveTo(padL + plotW * (first.first / intervalH.toFloat()),
                   padT + plotH * (1f - first.second / cMax))
            points.drop(1).forEach { (t, c) ->
                lineTo(padL + plotW * (t / intervalH.toFloat()),
                       padT + plotH * (1f - c / cMax))
            }
        }
        drawPath(linePath, color = primary, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

        // X-axis tick marks at 0, τ/2, τ (labels drawn as Compose Text outside Canvas)
        val tickY = padT + plotH
        listOf(0f, 0.5f, 1f).forEach { frac ->
            val x = padL + plotW * frac
            drawLine(gridColor, Offset(x, tickY), Offset(x, tickY + 6f), strokeWidth = 1.5f)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// Shared atoms
// ════════════════════════════════════════════════════════════════════════════
