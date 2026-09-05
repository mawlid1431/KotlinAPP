package com.aiu.tdminsight.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.edit
import kotlinx.coroutines.flow.combine

/**
 * App-level user preferences, held in a CompositionLocal so any screen can
 * read or mutate the current theme. Defaults to "system" (follow OS).
 *
 * Values are mirrored into SharedPreferences: without that the disclaimer
 * reappears and the chosen theme is lost on every cold start.
 */
enum class ThemePref { SYSTEM, LIGHT, DARK }

private const val PREFS_NAME = "tdm_user_prefs"
private const val KEY_THEME = "theme"
private const val KEY_DISCLAIMER = "disclaimer_accepted"

class UserPrefs(private val store: SharedPreferences? = null) {

    val theme: MutableState<ThemePref> = mutableStateOf(
        store?.getString(KEY_THEME, null)
            ?.let { saved -> ThemePref.entries.firstOrNull { it.name == saved } }
            ?: ThemePref.SYSTEM
    )

    val disclaimerAccepted: MutableState<Boolean> = mutableStateOf(
        store?.getBoolean(KEY_DISCLAIMER, false) ?: false
    )

    /** Writes the current values back to disk. */
    fun persist() {
        store?.edit {
            putString(KEY_THEME, theme.value.name)
            putBoolean(KEY_DISCLAIMER, disclaimerAccepted.value)
        }
    }
}

val LocalUserPrefs = compositionLocalOf { UserPrefs() }

/** Convenience for screens that only need a one-off read of the theme pref. */
val LocalThemePref = compositionLocalOf<ThemePref> { ThemePref.SYSTEM }

/**
 * Builds the preferences from disk and keeps disk in step with every change,
 * so a restart restores what the user last chose.
 */
@Composable
fun rememberUserPrefs(): UserPrefs {
    val context = LocalContext.current
    val prefs = remember(context) {
        UserPrefs(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    }
    LaunchedEffect(prefs) {
        combine(
            snapshotFlow { prefs.theme.value },
            snapshotFlow { prefs.disclaimerAccepted.value },
        ) { _, _ -> }.collect { prefs.persist() }
    }
    return prefs
}
