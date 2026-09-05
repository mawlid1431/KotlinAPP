package com.aiu.tdminsight.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.aiu.tdminsight.auth.AuthState
import com.aiu.tdminsight.ui.theme.TdmNumericMono
import com.aiu.tdminsight.viewmodel.AuthViewModel

// ════════════════════════════════════════════════════════════════════════════
// PROFILE — the signed-in Clerk user, plus their Supabase profile row
// ════════════════════════════════════════════════════════════════════════════

/**
 * Avatar for the signed-in user.
 *
 * Uses the Clerk `image_url` when the account has one (Google sign-in almost
 * always does) and falls back to the user's initials on a themed circle, so
 * the slot is never empty and never shows a stock placeholder face.
 */
@Composable
internal fun UserAvatar(
    imageUrl: String?,
    initials: String,
    size: androidx.compose.ui.unit.Dp,
) {
    val shape = CircleShape
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { InitialsBadge(initials, size) },
                error   = { InitialsBadge(initials, size) },
            )
        } else {
            InitialsBadge(initials, size)
        }
    }
}

@Composable
private fun InitialsBadge(initials: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value / 2.6f).sp,
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    nav: NavController,
    authVm: AuthViewModel,
) {
    val authState by authVm.authState.collectAsState()
    val profile   by authVm.profile.collectAsState()
    val user = authState as? AuthState.Authenticated

    // Pick up any change made in Clerk since sign-in (name, avatar, email).
    LaunchedEffect(Unit) { authVm.refreshProfile() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
    ) { pad ->
        if (user == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "You are not signed in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Identity header ───────────────────────────────────────────
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UserAvatar(user.imageUrl, user.initials, size = 96.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    user.fullName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(26.dp))

            // ── Account details (from Clerk) ──────────────────────────────
            SectionLabel("ACCOUNT")
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    ProfileRow("Full name", user.fullName)
                    ProfileDivider()
                    ProfileRow("Email", user.email)
                    ProfileDivider()
                    ProfileRow("First name", user.firstName ?: "Not set in Clerk")
                    ProfileDivider()
                    ProfileRow("Last name", user.lastName ?: "Not set in Clerk")
                    ProfileDivider()
                    ProfileRow("Clerk user ID", user.userId, mono = true)
                }
            }

            Spacer(Modifier.height(26.dp))

            // ── Application profile (from Supabase) ───────────────────────
            SectionLabel("APPLICATION PROFILE")
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    val p = profile
                    if (p == null) {
                        ProfileRow("Status", "Not synced yet — check your connection")
                    } else {
                        ProfileRow("Role", p.role ?: "student")
                        ProfileDivider()
                        ProfileRow("Institution", p.institution ?: "Not set")
                        ProfileDivider()
                        ProfileRow("Department", p.department ?: "Not set")
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Your name, email and picture come from your Clerk account. " +
                    "Role, institution and department are stored with your app profile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(34.dp))
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String, mono: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
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
            style = if (mono) TdmNumericMono.copy(fontSize = 12.sp)
                    else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f),
        )
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
}
