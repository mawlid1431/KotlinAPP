package com.aiu.tdminsight.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary               = L_Primary,
    onPrimary             = L_OnPrimary,
    primaryContainer      = L_PrimaryContainer,
    onPrimaryContainer    = L_OnPrimaryContainer,
    surface               = L_Surface,
    surfaceDim            = L_SurfaceDim,
    surfaceContainer      = L_SurfaceContainer,
    surfaceContainerHigh  = L_SurfaceContainerHigh,
    background            = L_Background,
    onBackground          = L_OnBackground,
    onSurface             = L_OnSurface,
    onSurfaceVariant      = L_OnSurfaceVariant,
    outline               = L_Outline,
    outlineVariant        = L_OutlineVariant,
    error                 = L_Error,
    onError               = L_OnError,
    errorContainer        = L_ErrorContainer,
    onErrorContainer      = L_OnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary               = D_Primary,
    onPrimary             = D_OnPrimary,
    primaryContainer      = D_PrimaryContainer,
    onPrimaryContainer    = D_OnPrimaryContainer,
    surface               = D_Surface,
    surfaceDim            = D_SurfaceDim,
    surfaceContainer      = D_SurfaceContainer,
    surfaceContainerHigh  = D_SurfaceContainerHigh,
    background            = D_Background,
    onBackground          = D_OnBackground,
    onSurface             = D_OnSurface,
    onSurfaceVariant      = D_OnSurfaceVariant,
    outline               = D_Outline,
    outlineVariant        = D_OutlineVariant,
    error                 = D_Error,
    onError               = D_OnError,
    errorContainer        = D_ErrorContainer,
    onErrorContainer      = D_OnErrorContainer,
)

/**
 * App-wide theme. Defaults to system dark/light. Pass [darkTheme] explicitly
 * from the Settings screen override.
 */
@Composable
fun TDMInsightTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extended    = if (darkTheme) DarkExtended else LightExtended

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Walk the ContextWrapper chain instead of casting directly: the
            // view's context is not always the Activity (dialogs, ComposeView
            // hosts), and a hard cast would throw ClassCastException there.
            val window = view.context.findActivity()?.window
            if (window != null) {
                @Suppress("DEPRECATION") // Kept for API < 35, where edge-to-edge
                // does not colour the status bar for us. Removing it would change
                // the app's appearance on older devices.
                run { window.statusBarColor = colorScheme.background.toArgb() }
                WindowCompat.getInsetsController(window, view)
                    .isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = TdmTypography,
            shapes      = TdmShapes,
            content     = content,
        )
    }
}

/** Finds the hosting Activity, or null when this view is not inside one. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity       -> this
    is ContextWrapper -> baseContext.findActivity()
    else              -> null
}
