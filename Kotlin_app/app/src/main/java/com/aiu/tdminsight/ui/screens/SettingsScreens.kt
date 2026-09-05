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
import com.aiu.tdminsight.viewmodel.AccountDeletionState
import com.aiu.tdminsight.viewmodel.HistoryViewModel

// ==========================================================================
// SettingsScreens — Settings (account, appearance) and the about/disclaimer screen.
// ==========================================================================

// ════════════════════════════════════════════════════════════════════════════
// 18 · SETTINGS  (Fitness profile style)
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    nav: NavController,
    historyVm: HistoryViewModel,
    // Supplied by TdmNavGraph from the Activity-scoped instance. Do NOT default
    // this to viewModel(): inside a NavHost that yields a per-destination copy,
    // so signing out would update a ViewModel nobody is listening to.
    authVm: com.aiu.tdminsight.viewmodel.AuthViewModel,
) {
    val prefs = LocalUserPrefs.current
    val theme by prefs.theme
    val authState by authVm.authState.collectAsState()
    val deletion  by authVm.accountDeletion.collectAsState()
    var confirmDelete by remember { mutableStateOf(false) }

    val savedCases by historyVm.entries.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }
    var clearError   by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { historyVm.load() }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            icon = {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("Clear saved cases?") },
            text = {
                Text(
                    "This permanently deletes your ${savedCases.size} saved " +
                        "${if (savedCases.size == 1) "case" else "cases"}. " +
                        "Your account stays active. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    historyVm.clearAllCases { ok ->
                        clearError = if (ok) null else "Could not clear your cases. Please try again."
                    }
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }

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

            if (authState is com.aiu.tdminsight.auth.AuthState.Authenticated) {
                val user = authState as com.aiu.tdminsight.auth.AuthState.Authenticated
                Spacer(Modifier.height(26.dp))
                SectionLabel("ACCOUNT")
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Identity — real Clerk avatar, name and email.
                        Row(
                            Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            UserAvatar(user.imageUrl, user.initials, size = 44.dp)
                            Column(Modifier.weight(1f)) {
                                Text(user.fullName, style = MaterialTheme.typography.titleSmall)
                                Text(user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { nav.navigate(Routes.PROFILE) }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Outlined.Person, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp))
                            Text("Profile",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Row(
                            Modifier.fillMaxWidth().clickable { authVm.signOut() }.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp))
                            Text("Sign out",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable(enabled = deletion !is AccountDeletionState.InProgress) {
                                    confirmDelete = true
                                }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp))
                            Text("Delete account",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f))
                            if (deletion is AccountDeletionState.InProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }

                (deletion as? AccountDeletionState.Failed)?.let { failed ->
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            failed.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }

                if (confirmDelete) {
                    AlertDialog(
                        onDismissRequest = { confirmDelete = false },
                        icon = {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error)
                        },
                        title = { Text("Delete your account?") },
                        text = {
                            Text(
                                "This permanently deletes your TDM Insight account, " +
                                    "your saved cases and your profile. " +
                                    "It cannot be undone.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                confirmDelete = false
                                authVm.deleteAccount()
                            }) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
                        },
                    )
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
                    Modifier
                        .padding(18.dp)
                        .clickable(enabled = savedCases.isNotEmpty()) { confirmClear = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Clear saved cases", style = MaterialTheme.typography.titleSmall)
                        // Real count, read from the same Supabase-backed list the
                        // History screen shows (was a hardcoded "No cases saved").
                        Text(
                            when (savedCases.size) {
                                0    -> "No cases saved"
                                1    -> "1 case saved"
                                else -> "${savedCases.size} cases saved"
                            },
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
internal fun SectionLabel(text: String) {
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
