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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

// ════════════════════════════════════════════════════════════════════════════
// 01 · SPLASH
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun SplashScreen(nav: NavController) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1800)
        nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Bottom-right decorative circle (ZEN. style abstract shape)
        Box(
            Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
        )
        Box(
            Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
        )

        // "TDM" chip top-left (ZEN. "ZEN." label)
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp, 56.dp)
        ) {
            Text(
                "TDM",
                style = TdmNumericMono.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }

        // Hero text bottom-left (ZEN. "Find your inner calm.")
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 100.dp)
        ) {
            Text(
                "START YOUR JOURNEY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Vancomycin\nTherapeutic\nDrug Monitoring",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 44.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "TDM Insight",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Loading bar at bottom
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )

        Text(
            "ACADEMIC PROTOTYPE · FICTIONAL DATA",
            style = TdmNumericMono.copy(letterSpacing = 0.8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 02 · HOME  (Fitness dashboard style)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun HomeScreen(nav: NavController, vm: CaseViewModel = viewModel()) {
    Scaffold(
        bottomBar = { BottomNavBar(current = Routes.HOME) { nav.navigate(it) } },
        containerColor = MaterialTheme.colorScheme.background,
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // Header row (Fitness "Good morning!" style)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "TDM Insight",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Vancomycin therapeutic drug monitoring",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                ThemeToggleButton()
            }

            Spacer(Modifier.height(24.dp))

            // Hero CTA card (ZEN. "Find your inner calm" + Fitness dashboard card)
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(22.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                    ) {
                        Text(
                            "NEW CASE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Start a new\ncalculation",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Enter a fictional patient case and derive PK parameters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f)
                    )
                    Spacer(Modifier.height(20.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .height(52.dp)
                            .fillMaxWidth()
                            .clickable { nav.navigate(Routes.NEW_CASE) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Start new case",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Quick shortcuts row (Fitness "Weekly Workouts" / "Calories" tiles)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeShortcutTile(
                    label = "Concentration\ncurve",
                    icon = Icons.AutoMirrored.Outlined.ShowChart,
                    modifier = Modifier.weight(1f)
                )
                HomeShortcutTile(
                    label = "Scan lab\nreport",
                    icon = Icons.Outlined.DocumentScanner,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(26.dp))

            // Recent section header
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT CALCULATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "See all",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { nav.navigate(Routes.HISTORY) }
                )
            }

            Spacer(Modifier.height(12.dp))
            RecentCasesEmpty()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeToggleButton() {
    val prefs = LocalUserPrefs.current
    val cur by prefs.theme
    val isDark = when (cur) {
        com.aiu.tdminsight.ui.ThemePref.SYSTEM -> isSystemInDarkTheme()
        com.aiu.tdminsight.ui.ThemePref.LIGHT  -> false
        com.aiu.tdminsight.ui.ThemePref.DARK   -> true
    }
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .size(44.dp)
            .clickable {
                prefs.theme.value =
                    if (isDark) com.aiu.tdminsight.ui.ThemePref.LIGHT
                    else com.aiu.tdminsight.ui.ThemePref.DARK
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = "Toggle theme",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HomeShortcutTile(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecentCasesEmpty() {
    Surface(
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(28.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "No calculations yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Cases you calculate appear here.\nEverything stays on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// Bottom navigation bar (Fitness app style)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun BottomNavBar(current: String, onNav: (String) -> Unit) {
    val items = listOf(
        Triple(Routes.HOME,     Icons.Filled.Home,     "Home"),
        Triple(Routes.HISTORY,  Icons.Filled.History,  "History"),
        Triple(Routes.SETTINGS, Icons.Filled.Settings, "Settings"),
    )
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            Modifier
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, icon, label) ->
                val selected = current == route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNav(route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(
                            Modifier.padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
                NumField("Weight", s.patient.weightKg, "kg", Modifier.weight(1f)) {
                    vm.updatePatient(s.patient.copy(weightKg = it)) }
                NumField("Height", s.patient.heightCm, "cm", Modifier.weight(1f)) {
                    vm.updatePatient(s.patient.copy(heightCm = it)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumField("Age", s.patient.ageLYears.toDouble(), "yr", Modifier.weight(1f)) {
                    vm.updatePatient(s.patient.copy(ageLYears = it.toInt().coerceAtLeast(0))) }
                SexPicker(s.patient.isMale, Modifier.weight(1f)) {
                    vm.updatePatient(s.patient.copy(isMale = it)) }
            }
            NumField("Serum creatinine", s.patient.scrUmolL, "µmol/L") {
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
                NumField("Dose administered", s.dosing.doseMg, "mg") {
                    vm.updateDosing(s.dosing.copy(doseMg = it)) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumField("Infusion duration", s.dosing.infusionDurationHours, "hours",
                        Modifier.weight(1f)) {
                        vm.updateDosing(s.dosing.copy(infusionDurationHours = it)) }
                    NumField("Dosing interval τ", s.dosing.intervalHours, "hours",
                        Modifier.weight(1f)) {
                        vm.updateDosing(s.dosing.copy(intervalHours = it)) }
                }
            }

            if (w == VancoWorkflow.PRE || w == VancoWorkflow.PRE_POST) {
                FormGroup("Pre-dose sample") {
                    NumField("Pre-dose (trough) concentration",
                        s.pre.preDoseConcentration, "mg/L") {
                        vm.updatePreSample(s.pre.copy(preDoseConcentration = it)) }
                    NumField("Pre-dose sample time",
                        s.pre.hoursBeforeDose, "h after dose start") {
                        vm.updatePreSample(s.pre.copy(hoursBeforeDose = it)) }
                }
            }

            if (w == VancoWorkflow.POST || w == VancoWorkflow.PRE_POST) {
                FormGroup("Post-dose sample") {
                    NumField("Post-dose concentration",
                        s.post.postDoseConcentration, "mg/L") {
                        vm.updatePostSample(s.post.copy(postDoseConcentration = it)) }
                    NumField("Post-dose sample time",
                        s.post.hoursAfterEndOfInfusion, "h after dose start") {
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
                    IconButton(onClick = { }) {
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

// ════════════════════════════════════════════════════════════════════════════
// 18 · SETTINGS  (Fitness profile style)
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val prefs = LocalUserPrefs.current
    val theme by prefs.theme
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        bottomBar = { BottomNavBar(current = Routes.SETTINGS) { nav.navigate(it) } },
        containerColor = MaterialTheme.colorScheme.background
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Profile / app-info header (Fitness profile card style)
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Outlined.MedicalServices, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(14.dp).size(26.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("TDM Insight",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("v0.4.0 · Academic prototype",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            // Appearance section
            SectionLabel("APPEARANCE")
            Spacer(Modifier.height(10.dp))
            SegmentedThemeRow(theme) { prefs.theme.value = it }
            Spacer(Modifier.height(6.dp))
            Text("Defaults to your Android system theme.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(26.dp))

            // Data section
            SectionLabel("DATA")
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(18.dp).clickable { },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Clear saved cases", style = MaterialTheme.typography.titleSmall)
                        Text("No cases saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp))
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(26.dp))

            // Information section
            SectionLabel("INFORMATION")
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoRow("About & disclaimer",
                    "TDM Insight · v0.4.0 · build 2026.08",
                    icon = Icons.Outlined.Info,
                    onClick = { nav.navigate(Routes.DISCLAIMER) })
            }

            Spacer(Modifier.height(24.dp))

            // Disclaimer banner (Fitness "Go Premium" card style)
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null,
                            tint = MaterialTheme.tdm.warning)
                        Text("Clinical disclaimer",
                            style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "TDM Insight is an academic prototype. All cases are fictional. Output is a teaching aid — never use for real prescribing decisions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("v0.4.0 · build 2026.08",
                        style = TdmNumericMono.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SegmentedThemeRow(
    current: com.aiu.tdminsight.ui.ThemePref,
    onPick: (com.aiu.tdminsight.ui.ThemePref) -> Unit
) {
    val items = listOf(
        com.aiu.tdminsight.ui.ThemePref.LIGHT  to "Light",
        com.aiu.tdminsight.ui.ThemePref.DARK   to "Dark",
        com.aiu.tdminsight.ui.ThemePref.SYSTEM to "System"
    )
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(4.dp)) {
            items.forEach { (k, label) ->
                val sel = current == k
                Box(
                    Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (sel) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onPick(k) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (sel) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    title: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(sub, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(20.dp))
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 19 · ABOUT & DISCLAIMER
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimerScreen(nav: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About & disclaimer") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Brand card
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Icon(Icons.Outlined.MedicalServices, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(14.dp).size(24.dp))
                    }
                    Column {
                        Text("TDM Insight",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("v0.4.0 · Academic prototype",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            AboutSection("Disclaimer",
                "TDM Insight is an academic software prototype developed for course " +
                "CDE2313 at Albukhary International University. It is intended only for " +
                "educational and software development purposes. It must not be used as a " +
                "clinically validated prescribing, diagnostic, or treatment-decision tool. " +
                "All demonstration cases must be fictional.")
            AboutSection("Method note",
                "Vancomycin AUC-guided dosing, Rybak 2020 target 400–600 mg·h/L. " +
                "Pre and Post workflows use population Vd priors; Pre+Post uses " +
                "Sawchuk-Zaske two-point log-linear regression.")
            AboutSection("Course & institution",
                "CDE2313 — Mobile Application Development\n" +
                "Albukhary International University (AIU)\n" +
                "Semester 5, 2026")
            AboutSection("References",
                "• Rybak et al., AJHP 2020 — AUC₂₄ 400–600 mg·h/L target\n" +
                "• Cockcroft & Gault, Nephron 1976 — CrCl\n" +
                "• Sawchuk & Zaske, J Pharmacokinet Biopharm 1976 — two-point method\n" +
                "• Malaysian PhIS TDM Calculator — workflow reference")
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AboutSection(title: String, body: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// 17 · HISTORY  (ZEN. favorites list style)
// ════════════════════════════════════════════════════════════════════════════
// History — Supabase data (falls back to demo entries) + PK curve graph
// ════════════════════════════════════════════════════════════════════════════

internal data class HistoryEntry(
    val caseId: String,
    val date: String,
    val workflow: VancoWorkflow,
    val doseMg: Double,
    val intervalH: Double,
    val tInfH: Double,
    val auc24: Double,
    val recDoseMg: Double,
    val ke: Double,
    val t12: Double,
    val vdL: Double,
    val clLH: Double,
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    nav: NavController,
    vm: com.aiu.tdminsight.viewmodel.HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val entries   by vm.entries.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val isLive    by vm.isLiveData.collectAsState()

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
                if (isLive) "Your saved calculations" else "Demo cases — run a calculation to save your own",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                entries.forEach { entry ->
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

// ZEN. style selection card with radio circle
@Composable
private fun ZenSelectionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.MedicalServices,
    onClick: () -> Unit
) {
    Surface(
        onClick = if (enabled) onClick else { {} },
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = when {
            selected -> MaterialTheme.colorScheme.primaryContainer
            !enabled -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
            else     -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            when {
                selected -> MaterialTheme.colorScheme.primary
                !enabled -> MaterialTheme.colorScheme.outlineVariant
                else     -> MaterialTheme.colorScheme.outline
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left icon circle (unique per option, like ZEN. app)
            Surface(
                shape = CircleShape,
                color = if (enabled) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(icon, contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(10.dp).size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.outlineVariant)
                Text(subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp))
            }
            // ZEN. radio circle
            ZenRadioCircle(selected && enabled)
        }
    }
}

@Composable
private fun ZenRadioCircle(selected: Boolean) {
    Box(
        Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .border(
                if (selected) 0.dp else 2.dp,
                if (selected) Color.Transparent else MaterialTheme.colorScheme.outline,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp))
        }
    }
}

// WizardTopBar (ZEN. progress bar style)
@Composable
private fun WizardTopBar(
    title: String, current: Int, total: Int,
    caseId: String, onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back",
                        modifier = Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text("Step $current of $total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 1.dp))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Text(caseId, style = TdmNumericMono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
            // ZEN. style progress segments
            StepIndicator(current = current, total = total,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f))
        Text(value,
            style = TdmNumericMono.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface)
    }
    Box(Modifier.fillMaxWidth().height(1.dp)
        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)))
}

@Composable
private fun TextField6(label: String, value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = {
                Text(
                    "Enter ${label.lowercase().replace(" (fictional)", "")}…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            )
        )
    }
}

@Composable
private fun NumField(
    label: String, value: Double, unit: String,
    modifier: Modifier = Modifier, onChange: (Double) -> Unit
) {
    var text by remember { mutableStateOf(if (value == 0.0) "" else value.toString()) }
    val lastExternal = remember { mutableStateOf(value) }
    if (value != lastExternal.value && value != (text.toDoubleOrNull() ?: 0.0)) {
        text = if (value == 0.0) "" else value.toString()
    }
    lastExternal.value = value
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = text,
            onValueChange = { v ->
                text = v.filter { it.isDigit() || it == '.' }
                onChange(text.toDoubleOrNull() ?: 0.0)
            },
            placeholder = {
                Text(
                    "0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            },
            trailingIcon = {
                Text(
                    unit,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 14.dp)
                )
            },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            )
        )
    }
}

@Composable
private fun SexPicker(isMale: Boolean, modifier: Modifier = Modifier, onChange: (Boolean) -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Sex",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(Modifier.padding(4.dp)) {
                listOf(true to "Male", false to "Female").forEach { (male, label) ->
                    val selected = isMale == male
                    Box(
                        Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.onSurface
                                else Color.Transparent
                            )
                            .clickable { onChange(male) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

