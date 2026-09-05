package com.aiu.tdminsight

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import com.aiu.tdminsight.auth.AuthState
import com.aiu.tdminsight.ui.LocalUserPrefs
import com.aiu.tdminsight.ui.ThemePref
import com.aiu.tdminsight.ui.UserPrefs
import com.aiu.tdminsight.ui.rememberUserPrefs
import com.aiu.tdminsight.ui.navigation.TdmNavGraph
import com.aiu.tdminsight.ui.screens.LoginScreen
import com.aiu.tdminsight.ui.screens.SignUpScreen
import com.aiu.tdminsight.ui.screens.SplashScreen
import com.aiu.tdminsight.ui.screens.WelcomeScreen
import com.aiu.tdminsight.ui.theme.AuthBackgroundGradient
import com.aiu.tdminsight.ui.theme.AuthBlobBlue
import com.aiu.tdminsight.ui.theme.AuthBlobPurple
import com.aiu.tdminsight.ui.theme.AuthInk
import com.aiu.tdminsight.ui.theme.TDMInsightTheme
import com.aiu.tdminsight.ui.theme.TdmNumericMono
import com.aiu.tdminsight.ui.theme.tdm
import com.aiu.tdminsight.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    /**
     * Set when Clerk's OAuth redirect (tdminsight://oauth-callback) brings the
     * browser back into the app. Compose observes this and finishes the sign-in.
     */
    private val oauthCallback = MutableStateFlow<Uri?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureOAuthRedirect(intent)
    }

    private fun captureOAuthRedirect(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "tdminsight" && data.host == "oauth-callback") {
            oauthCallback.value = data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Covers the case where the app was killed while the browser was open.
        captureOAuthRedirect(intent)
        enableEdgeToEdge()
        setContent {
            val prefs = rememberUserPrefs()
            CompositionLocalProvider(LocalUserPrefs provides prefs) {
                val theme by prefs.theme
                val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
                val darkTheme = when (theme) {
                    ThemePref.SYSTEM -> systemDark
                    ThemePref.LIGHT  -> false
                    ThemePref.DARK   -> true
                }
                TDMInsightTheme(darkTheme = darkTheme) {
                    val accepted by prefs.disclaimerAccepted
                    val authVm: AuthViewModel = viewModel()
                    val authState by authVm.authState.collectAsState()
                    val freshLogin by authVm.freshLogin.collectAsState()
                    var showSignUp by remember { mutableStateOf(false) }
                    var splashDone by remember { mutableStateOf(false) }

                    // Clerk handed us a Google consent URL -> open the browser.
                    val context = LocalContext.current
                    val oauthUrl by authVm.pendingOAuthUrl.collectAsState()
                    LaunchedEffect(oauthUrl) {
                        val url = oauthUrl ?: return@LaunchedEffect
                        authVm.consumeOAuthUrl()
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }

                    // Browser came back via the deep link -> finish the sign-in.
                    val callback by oauthCallback.collectAsState()
                    LaunchedEffect(callback) {
                        val uri = callback
                        if (uri != null) {
                            oauthCallback.value = null
                            // Hand the WHOLE deep link to Clerk: its query string
                            // carries the rotated device token for this session.
                            authVm.completeGoogleSignIn(uri.toString())
                        }
                    }

                    android.util.Log.d("TdmAuth", "splashDone=$splashDone, accepted=$accepted, isConfigured=${authVm.isConfigured}, authState=$authState, freshLogin=$freshLogin")

                    when {
                        // 1. Splash screen always shows first on every launch
                        !splashDone -> SplashScreen { splashDone = true }
                        // 2. First-ever launch: show the "Get Started" disclaimer
                        !accepted -> FirstLaunchDisclaimer {
                            prefs.disclaimerAccepted.value = true
                        }
                        // 3. Skip auth gate when Clerk is not configured (dev mode)
                        !authVm.isConfigured -> TdmNavGraph(authVm = authVm)
                        // 4. Fresh login/signup: show welcome screen once, then go to app
                        //    (AuthState.Loading is NOT intercepted here: the saved
                        //    session is restored synchronously, so Loading only means
                        //    a sign-in is in flight — the login screen shows its own
                        //    button spinner for that, which beats a blank screen.)
                        authState is AuthState.Authenticated && freshLogin -> {
                            val auth = authState as AuthState.Authenticated
                            WelcomeScreen(
                                isNewUser  = auth.isNewUser,
                                email      = auth.email,
                                onContinue = { authVm.consumeWelcome() },
                            )
                        }
                        // 5. Already authenticated (session restore or after welcome)
                        authState is AuthState.Authenticated -> TdmNavGraph(authVm = authVm)
                        // 6. Sign-up screen
                        showSignUp -> SignUpScreen(
                            authState = authState,
                            onSignUp  = { email, pw -> authVm.signUp(email, pw) },
                            onGoogleSignIn = { authVm.startGoogleSignIn() },
                            onGoToLogin = { showSignUp = false; authVm.clearError() },
                        )
                        // 7. Login screen (Clerk + Google)
                        else -> LoginScreen(
                            authState  = authState,
                            onSignIn   = { email, pw -> authVm.signIn(email, pw) },
                            onGoogleSignIn = { authVm.startGoogleSignIn() },
                            onGoToSignUp = { showSignUp = true; authVm.clearError() },
                        )
                    }
                }
            }
        }
    }
}

// ZEN. "Begin your practice" style — dark cosmic background, bold heading, CTA buttons
@Composable
private fun FirstLaunchDisclaimer(onAccept: () -> Unit) {
    // Dark deep-navy gradient background (ZEN. "Begin your practice" screen)
    val bgGradient = AuthBackgroundGradient
    // Accent decorative blobs (like ZEN. cosmic background)
    val blob1 = AuthBlobBlue.copy(alpha = 0.25f)
    val blob2 = AuthBlobPurple.copy(alpha = 0.18f)

    Box(
        Modifier
            .fillMaxSize()
            .background(brush = bgGradient)
    ) {
        // Decorative blobs
        Box(
            Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(blob1)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
        )
        Box(
            Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(blob2)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 60.dp)
        )

        // Content
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top logo mark
            Spacer(Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Icon(
                        Icons.Outlined.MedicalServices,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Text(
                    "TDM Insight",
                    style = TdmNumericMono.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Hero heading (ZEN. "Begin your practice")
            Column {
                Text(
                    "Begin your\npractice",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        lineHeight = 44.sp
                    ),
                    color = Color.White
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Academic prototype for CDE2313.\nAll cases must be fictional.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }

            // CTA buttons (ZEN. Apple / Google / Email style)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Primary CTA — "I understand" (matches ZEN. Apple/Google button)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .clickable { onAccept() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "I understand — get started",
                        style = MaterialTheme.typography.titleSmall,
                        color = AuthInk
                    )
                }

                // Secondary info row (ZEN. "Continue with Email" style)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Mail,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Not for real patient data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Disclaimer footer text
                Text(
                    "TDM Insight is an academic software prototype (CDE2313 · AIU).\n" +
                    "Output is a teaching aid — never use for real prescribing decisions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.40f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
