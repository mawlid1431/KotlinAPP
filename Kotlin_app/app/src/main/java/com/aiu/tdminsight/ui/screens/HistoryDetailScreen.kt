package com.aiu.tdminsight.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aiu.tdminsight.data.model.Auc24Verdict
import com.aiu.tdminsight.data.model.HistoryEntry
import com.aiu.tdminsight.data.model.VancoWorkflow
import com.aiu.tdminsight.ui.export.ShareCaseReport
import com.aiu.tdminsight.ui.theme.tdm
import com.aiu.tdminsight.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch

// ==========================================================================
// HistoryDetailScreen — one saved case in full, with the option to export it
// as a PDF and share it. Reuses the app's existing Material 3 theme, shapes
// and typography; it introduces no new design language.
// ==========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    nav: NavController,
    vm: HistoryViewModel = viewModel(),
) {
    val entry by vm.selected.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var sharing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Case detail") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val e = entry
                    IconButton(
                        enabled = e != null && !sharing,
                        onClick = {
                            if (e != null) {
                                sharing = true
                                scope.launch {
                                    val ok = ShareCaseReport.share(context, e, vm.reportAuthor())
                                    sharing = false
                                    if (!ok) snackbar.showSnackbar("Could not create the PDF report.")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.IosShare, contentDescription = "Share as PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        val e = entry
        if (e == null) {
            // Reached with no selection (e.g. process death restored the route).
            Box(
                Modifier.fillMaxSize().padding(p),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "This case is no longer available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { nav.popBackStack() }) { Text("Back to history") }
                }
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Identity ──────────────────────────────────────────────────
            Text(
                e.caseId,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                workflowText(e.workflow),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            // Real creation time from the saved row, in the reader's time zone.
            Text(
                "Created ${createdAtText(e)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(Modifier.height(18.dp))

            // ── Headline result ───────────────────────────────────────────
            DetailCard {
                val statusColor = verdictColor(e.verdict)
                Text(
                    "AUC₂₄",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${"%.0f".format(e.auc24)} mg·h/L",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        "${verdictText(e.verdict)}  ·  target 400–600 mg·h/L",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                TargetBandBar(auc24 = e.auc24, color = statusColor)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailStat("Recommended dose", "${"%.0f".format(e.recDoseMg)} mg", Modifier.weight(1f))
                    DetailStat("Interval", "every ${"%.0f".format(e.intervalH)} h", Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Concentration–time curve ──────────────────────────────────
            DetailCard {
                Text(
                    "Concentration–time curve",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "One dosing interval, hours after the start of the dose",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                DetailCurve(e, modifier = Modifier.fillMaxWidth().height(170.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0 h", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.0f".format(e.intervalH / 2)} h", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.0f".format(e.intervalH)} h", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Pharmacokinetic parameters ────────────────────────────────
            DetailCard {
                DetailSectionLabel("Pharmacokinetic parameters")
                DetailRow("Elimination rate constant (ke)", "%.4f".format(e.ke), "h⁻¹")
                DetailRow("Half-life (t½)", "%.2f".format(e.t12), "h")
                DetailRow("Volume of distribution (Vd)", "%.2f".format(e.vdL), "L")
                if (e.vdLPerKg > 0) DetailRow("Vd per kg", "%.3f".format(e.vdLPerKg), "L/kg")
                DetailRow("Clearance (CL)", "%.3f".format(e.clLH), "L/h")
                e.cmin?.let { DetailRow("Projected trough (Cmin)", "%.2f".format(it), "mg/L") }
                e.cmax?.let { DetailRow("Projected peak (Cmax)", "%.2f".format(it), "mg/L") }
            }

            Spacer(Modifier.height(14.dp))

            // ── Patient ───────────────────────────────────────────────────
            DetailCard {
                DetailSectionLabel("Patient")
                DetailRow("Weight", "%.1f".format(e.weightKg), "kg")
                DetailRow("Age", e.ageYears.toString(), "years")
                DetailRow("Sex", if (e.isMale) "Male" else "Female", "")
                DetailRow("Serum creatinine", "%.1f".format(e.scrUmolL), "µmol/L")
            }

            Spacer(Modifier.height(14.dp))

            // ── Regimen ───────────────────────────────────────────────────
            DetailCard {
                DetailSectionLabel("Dosing regimen")
                DetailRow("Dose", "%.0f".format(e.doseMg), "mg")
                DetailRow("Dosing interval (τ)", "%.1f".format(e.intervalH), "h")
                DetailRow("Infusion duration", "%.2f".format(e.tInfH), "h")
            }

            // ── Measured samples (only what this workflow used) ────────────
            val hasSamples = e.preConcMgL != null || e.postConcMgL != null
            if (hasSamples) {
                Spacer(Modifier.height(14.dp))
                DetailCard {
                    DetailSectionLabel("Measured concentrations")
                    e.preConcMgL?.let { DetailRow("Pre-dose (trough)", "%.2f".format(it), "mg/L") }
                    e.preTimeH?.let { DetailRow("Trough sample time", "%.2f".format(it), "h") }
                    e.postConcMgL?.let { DetailRow("Post-dose (peak)", "%.2f".format(it), "mg/L") }
                    e.postTimeH?.let { DetailRow("Peak sample time", "%.2f".format(it), "h") }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Share ─────────────────────────────────────────────────────
            Button(
                onClick = {
                    sharing = true
                    scope.launch {
                        val ok = ShareCaseReport.share(context, e, vm.reportAuthor())
                        sharing = false
                        if (!ok) snackbar.showSnackbar("Could not create the PDF report.")
                    }
                },
                enabled = !sharing,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (sharing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Preparing PDF…")
                } else {
                    Icon(Icons.Filled.IosShare, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Share as PDF")
                }
            }
            Text(
                "Creates a full PDF report of this case and opens the share sheet.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

// ── Local building blocks (kept private to this screen) ────────────────────

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@Composable
private fun DetailSectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun DetailRow(label: String, value: String, unit: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (unit.isNotBlank()) {
            Text(
                " $unit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Horizontal 0–800 scale with the 400–600 therapeutic window marked. */
@Composable
private fun TargetBandBar(auc24: Double, color: androidx.compose.ui.graphics.Color) {
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val band = MaterialTheme.tdm.success.copy(alpha = 0.18f)
    val axisMax = maxOf(800.0, auc24 * 1.15)

    Canvas(Modifier.fillMaxWidth().height(26.dp)) {
        val w = size.width
        val barH = 12f
        val top = (size.height - barH) / 2f
        fun px(v: Double) = (w * (v / axisMax)).toFloat().coerceIn(0f, w)

        drawRoundRect(
            color = track,
            topLeft = Offset(0f, top),
            size = androidx.compose.ui.geometry.Size(w, barH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        )
        drawRect(
            color = band,
            topLeft = Offset(px(400.0), top),
            size = androidx.compose.ui.geometry.Size(px(600.0) - px(400.0), barH),
        )
        val mx = px(auc24)
        drawLine(color, Offset(mx, top - 5f), Offset(mx, top + barH + 5f),
            strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

/** Same one-compartment model the History card plots, drawn a little larger. */
@Composable
private fun DetailCurve(e: HistoryEntry, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    val tau = if (e.intervalH > 0) e.intervalH else 12.0
    val tInf = if (e.tInfH > 0) e.tInfH else 1.0

    val points = remember(e.ke, e.vdL, e.doseMg, tau, tInf) {
        val steps = 140
        (0..steps).map { i ->
            val t = tau * i / steps
            val c = if (e.ke < 1e-9 || e.vdL < 1e-9) 0.0
            else {
                val peak = (e.doseMg / (e.ke * e.vdL * tInf)) * (1.0 - kotlin.math.exp(-e.ke * tInf))
                if (t <= tInf) (e.doseMg / (e.ke * e.vdL * tInf)) * (1.0 - kotlin.math.exp(-e.ke * t))
                else peak * kotlin.math.exp(-e.ke * (t - tInf))
            }
            t.toFloat() to c.toFloat()
        }
    }
    val cMax = points.maxOf { it.second }.coerceAtLeast(1f)

    Canvas(modifier) {
        val padL = 6f; val padR = 6f; val padT = 10f; val padB = 10f
        val plotW = size.width - padL - padR
        val plotH = size.height - padT - padB

        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
            val y = padT + plotH * (1f - frac)
            drawLine(grid, Offset(padL, y), Offset(size.width - padR, y), strokeWidth = 1f)
        }

        val infX = padL + plotW * (tInf / tau).toFloat()
        drawLine(grid, Offset(infX, padT), Offset(infX, padT + plotH), strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))

        fun x(t: Float) = padL + plotW * (t / tau.toFloat())
        fun y(c: Float) = padT + plotH * (1f - c / cMax)

        val fillPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(x(points.first().first), padT + plotH)
            points.forEach { (t, c) -> lineTo(x(t), y(c)) }
            lineTo(x(points.last().first), padT + plotH)
            close()
        }
        drawPath(fillPath, color = primary.copy(alpha = 0.10f))

        val linePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(x(points.first().first), y(points.first().second))
            points.drop(1).forEach { (t, c) -> lineTo(x(t), y(c)) }
        }
        drawPath(linePath, color = primary, style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

// ── Shared value helpers ──────────────────────────────────────────────────

@Composable
private fun verdictColor(v: Auc24Verdict) = when (v) {
    Auc24Verdict.IN_TARGET    -> MaterialTheme.tdm.success
    Auc24Verdict.ABOVE_TARGET -> MaterialTheme.colorScheme.error
    Auc24Verdict.BELOW_TARGET -> MaterialTheme.tdm.warning
    Auc24Verdict.INVALID      -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun verdictText(v: Auc24Verdict) = when (v) {
    Auc24Verdict.IN_TARGET    -> "In target"
    Auc24Verdict.ABOVE_TARGET -> "Above target"
    Auc24Verdict.BELOW_TARGET -> "Below target"
    Auc24Verdict.INVALID      -> "No result"
}

/** created_at from the database row, rendered in the device's time zone. */
private fun createdAtText(e: HistoryEntry): String {
    val iso = e.createdAtIso
    if (!iso.isNullOrBlank()) {
        return try {
            java.time.OffsetDateTime.parse(iso)
                .atZoneSameInstant(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy 'at' HH:mm"))
        } catch (_: Exception) {
            iso.take(16).replace('T', ' ')
        }
    }
    return e.date
}

private fun workflowText(w: VancoWorkflow) = when (w) {
    VancoWorkflow.PRE      -> "Pre-dose (trough only)"
    VancoWorkflow.POST     -> "Post-dose (peak only)"
    VancoWorkflow.PRE_POST -> "Pre + Post (trough and peak)"
}
