package com.aiu.tdminsight.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiu.tdminsight.auth.AuthState
import com.aiu.tdminsight.ui.theme.TdmNumericMono

// ── Shared background style (matches FirstLaunchDisclaimer in MainActivity) ──

private val AuthBg = Brush.verticalGradient(
    colors = listOf(Color(0xFF0A0E1A), Color(0xFF101524), Color(0xFF0F1117))
)
private val BlobBlue   = Color(0xFF3B6CC0).copy(alpha = 0.22f)
private val BlobPurple = Color(0xFF7B3FC4).copy(alpha = 0.16f)

@Composable
private fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(brush = AuthBg)
    ) {
        Box(
            Modifier.size(280.dp).clip(CircleShape)
                .background(BlobBlue)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-50).dp)
        )
        Box(
            Modifier.size(200.dp).clip(CircleShape)
                .background(BlobPurple)
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 50.dp)
        )
        content()
    }
}

// ── Auth text field (matches the existing NumField dark-on-dark style) ─────────

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    var showPassword by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White.copy(alpha = 0.75f),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (isPassword && !showPassword)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = Color.White.copy(alpha = 0.5f),
                        )
                    }
                }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor       = Color.White,
                unfocusedTextColor     = Color.White.copy(alpha = 0.9f),
                focusedBorderColor     = Color.White.copy(alpha = 0.6f),
                unfocusedBorderColor   = Color.White.copy(alpha = 0.2f),
                focusedContainerColor  = Color.White.copy(alpha = 0.06f),
                unfocusedContainerColor= Color.White.copy(alpha = 0.04f),
                cursorColor            = Color.White,
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Login screen ──────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    authState: AuthState,
    onSignIn: (email: String, password: String) -> Unit,
    onGoToSignUp: () -> Unit,
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading = authState is AuthState.Loading
    val errorMsg  = (authState as? AuthState.Error)?.message

    AuthBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Logo row ──────────────────────────────────────────────
            Spacer(Modifier.height(48.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.12f)) {
                    Icon(
                        Icons.Outlined.MedicalServices, contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp).size(22.dp),
                    )
                }
                Text(
                    "TDM Insight",
                    style = TdmNumericMono.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                )
            }

            // ── Heading ────────────────────────────────────────────────
            Column {
                Text(
                    "Sign in",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                    ),
                    color = Color.White,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Academic prototype · fictional cases only",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }

            // ── Form ───────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AuthField(
                    label = "Email address",
                    value = email,
                    onValueChange = { email = it },
                    keyboardType = KeyboardType.Email,
                )
                AuthField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    isPassword = true,
                )

                if (errorMsg != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFB00020).copy(alpha = 0.18f),
                    ) {
                        Text(
                            errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF8A80),
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Primary CTA
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .clickable(enabled = !isLoading) { onSignIn(email, password) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color(0xFF0A0E1A),
                            strokeWidth = 2.5.dp,
                        )
                    } else {
                        Text(
                            "Sign in",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF0A0E1A),
                        )
                    }
                }

                // Secondary — go to sign up
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(enabled = !isLoading) { onGoToSignUp() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Create an account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
            }

            // ── Footer ─────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "TDM Insight · CDE2313 · AIU\nOutput is a teaching aid only — not for real prescribing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

// ── Welcome screen (shown once after fresh login or sign-up) ─────────────────

@Composable
fun WelcomeScreen(
    isNewUser: Boolean,
    email: String,
    onContinue: () -> Unit,
) {
    val displayName = email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }
    val heading = if (isNewUser) "Welcome,\n$displayName!" else "Welcome back,\n$displayName!"
    val sub = if (isNewUser)
        "Your account is ready. Let's get started."
    else
        "Good to see you again. Your cases are waiting."

    AuthBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.height(48.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.12f)) {
                    Icon(
                        Icons.Outlined.MedicalServices, contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp).size(22.dp),
                    )
                }
                Text(
                    "TDM Insight",
                    style = TdmNumericMono.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                )
            }

            Column {
                Text(
                    heading,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        lineHeight = 44.sp,
                    ),
                    color = Color.White,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    sub,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .clickable { onContinue() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Continue to app  →",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF0A0E1A),
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ── Sign-up screen ────────────────────────────────────────────────────────────

@Composable
fun SignUpScreen(
    authState: AuthState,
    onSignUp: (email: String, password: String) -> Unit,
    onGoToLogin: () -> Unit,
) {
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var confirm   by remember { mutableStateOf("") }
    val isLoading  = authState is AuthState.Loading
    val errorMsg   = (authState as? AuthState.Error)?.message

    val localError = when {
        password.isNotBlank() && confirm.isNotBlank() && password != confirm ->
            "Passwords do not match."
        else -> null
    }

    AuthBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.height(48.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.12f)) {
                    Icon(
                        Icons.Outlined.MedicalServices, contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp).size(22.dp),
                    )
                }
                Text(
                    "TDM Insight",
                    style = TdmNumericMono.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                )
            }

            Column {
                Text(
                    "Create account",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold, fontSize = 34.sp,
                    ),
                    color = Color.White,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Academic prototype · fictional cases only",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AuthField("Email address", email, { email = it }, KeyboardType.Email)
                AuthField("Password", password, { password = it }, isPassword = true)
                AuthField("Confirm password", confirm, { confirm = it }, isPassword = true)

                val displayError = localError ?: errorMsg
                if (displayError != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFB00020).copy(alpha = 0.18f),
                    ) {
                        Text(
                            displayError,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF8A80),
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .clickable(enabled = !isLoading && localError == null) {
                            onSignUp(email, password)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color(0xFF0A0E1A),
                            strokeWidth = 2.5.dp,
                        )
                    } else {
                        Text(
                            "Create account",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF0A0E1A),
                        )
                    }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(enabled = !isLoading) { onGoToLogin() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Already have an account? Sign in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "TDM Insight · CDE2313 · AIU\nOutput is a teaching aid only — not for real prescribing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}
