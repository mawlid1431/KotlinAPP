package com.aiu.tdminsight.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Light palette — clean white + clinical blue (Uniwind-style clarity) ──────
internal val L_Primary             = Color(0xFF1464A8)   // medical blue
internal val L_OnPrimary           = Color(0xFFFFFFFF)
internal val L_PrimaryContainer    = Color(0xFFE3F0FF)   // very light blue tint
internal val L_OnPrimaryContainer  = Color(0xFF073A6B)
internal val L_Surface             = Color(0xFFFFFFFF)   // pure white cards
internal val L_SurfaceDim          = Color(0xFFF0F0F0)
internal val L_SurfaceContainer    = Color(0xFFF5F5F5)   // light gray containers
internal val L_SurfaceContainerHigh = Color(0xFFEEEEEE)
internal val L_Background          = Color(0xFFF8F8F8)   // near-white page background
internal val L_OnBackground        = Color(0xFF0F0F0F)   // near-black text
internal val L_OnSurface           = Color(0xFF0F0F0F)
internal val L_OnSurfaceVariant    = Color(0xFF6B6B6B)   // medium gray for secondary text
internal val L_Outline             = Color(0xFFE2E2E2)   // very light clean borders
internal val L_OutlineVariant      = Color(0xFFBCBCBC)
internal val L_Error               = Color(0xFFCC2929)
internal val L_OnError             = Color(0xFFFFFFFF)
internal val L_ErrorContainer      = Color(0xFFFFECEC)
internal val L_OnErrorContainer    = Color(0xFF8B0000)

internal val L_Warning             = Color(0xFF9A5F00)
internal val L_OnWarning           = Color(0xFFFFFFFF)
internal val L_WarningContainer    = Color(0xFFFFF3E0)
internal val L_OnWarningContainer  = Color(0xFF4A2C00)

internal val L_Success             = Color(0xFF1B6B3A)
internal val L_OnSuccess           = Color(0xFFFFFFFF)
internal val L_SuccessContainer    = Color(0xFFE8F5EE)
internal val L_OnSuccessContainer  = Color(0xFF00391A)

// ── Dark palette — clean near-black + bright clinical blue ────────────────
internal val D_Primary             = Color(0xFF60AAEE)   // bright readable blue
internal val D_OnPrimary           = Color(0xFF002244)
internal val D_PrimaryContainer    = Color(0xFF003870)
internal val D_OnPrimaryContainer  = Color(0xFFBEDCFF)
internal val D_Surface             = Color(0xFF181818)   // very dark card
internal val D_SurfaceDim          = Color(0xFF0A0A0A)
internal val D_SurfaceContainer    = Color(0xFF202020)
internal val D_SurfaceContainerHigh = Color(0xFF2A2A2A)
internal val D_Background          = Color(0xFF111111)   // near-black background
internal val D_OnBackground        = Color(0xFFEDEDED)   // near-white text
internal val D_OnSurface           = Color(0xFFEDEDED)
internal val D_OnSurfaceVariant    = Color(0xFF9A9A9A)   // readable secondary text
internal val D_Outline             = Color(0xFF303030)   // subtle borders
internal val D_OutlineVariant      = Color(0xFF464646)
internal val D_Error               = Color(0xFFFF5F5F)
internal val D_OnError             = Color(0xFF1A0000)
internal val D_ErrorContainer      = Color(0xFF450A0A)
internal val D_OnErrorContainer    = Color(0xFFFFCDD2)

internal val D_Warning             = Color(0xFFFFB74D)
internal val D_OnWarning           = Color(0xFF1A0E00)
internal val D_WarningContainer    = Color(0xFF3D2600)
internal val D_OnWarningContainer  = Color(0xFFFFE0B2)

internal val D_Success             = Color(0xFF5DD47B)
internal val D_OnSuccess           = Color(0xFF001A0A)
internal val D_SuccessContainer    = Color(0xFF0A3020)
internal val D_OnSuccessContainer  = Color(0xFFA8F0BE)

val WarningLight = L_Warning
val WarningDark  = D_Warning
val SuccessLight = L_Success
val SuccessDark  = D_Success

data class ExtendedColors(
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val surfaceContainerHigh: Color,
    val outlineVariant: Color,
)

val LocalExtendedColors = staticCompositionLocalOf<ExtendedColors> {
    error("ExtendedColors not provided. Wrap content in TDMInsightTheme {}.")
}

val LightExtended = ExtendedColors(
    warning               = L_Warning,
    onWarning             = L_OnWarning,
    warningContainer      = L_WarningContainer,
    onWarningContainer    = L_OnWarningContainer,
    success               = L_Success,
    onSuccess             = L_OnSuccess,
    successContainer      = L_SuccessContainer,
    onSuccessContainer    = L_OnSuccessContainer,
    surfaceContainerHigh  = L_SurfaceContainerHigh,
    outlineVariant        = L_OutlineVariant,
)

val DarkExtended = ExtendedColors(
    warning               = D_Warning,
    onWarning             = D_OnWarning,
    warningContainer      = D_WarningContainer,
    onWarningContainer    = D_OnWarningContainer,
    success               = D_Success,
    onSuccess             = D_OnSuccess,
    successContainer      = D_SuccessContainer,
    onSuccessContainer    = D_OnSuccessContainer,
    surfaceContainerHigh  = D_SurfaceContainerHigh,
    outlineVariant        = D_OutlineVariant,
)

val androidx.compose.material3.MaterialTheme.tdm: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

// ── Auth / onboarding palette ────────────────────────────────────────────────
// The sign-in, sign-up and first-launch screens share one fixed dark treatment
// that does not follow the light/dark theme. Defined once here so the shade is
// identical on every one of those screens.

/** Deep navy the auth screens sit on; also the text colour on white pills. */
val AuthInk = Color(0xFF0A0E1A)
internal val AuthInkMid = Color(0xFF101524)
internal val AuthInkLow = Color(0xFF0F1117)

/** Decorative background blobs. Alpha is chosen per screen. */
internal val AuthBlobBlue   = Color(0xFF3B6CC0)
internal val AuthBlobPurple = Color(0xFF7B3FC4)

/** Vertical gradient behind every auth / onboarding screen. */
val AuthBackgroundGradient = Brush.verticalGradient(
    colors = listOf(AuthInk, AuthInkMid, AuthInkLow)
)
