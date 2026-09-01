package com.aiu.tdminsight.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * App-level user preferences, held in a CompositionLocal so any screen can
 * read or mutate the current theme. Defaults to "system" (follow OS).
 */
enum class ThemePref { SYSTEM, LIGHT, DARK }

class UserPrefs {
    val theme: MutableState<ThemePref> = mutableStateOf(ThemePref.SYSTEM)
    val disclaimerAccepted: MutableState<Boolean> = mutableStateOf(false)
}

val LocalUserPrefs = compositionLocalOf { UserPrefs() }

/** Convenience for screens that only need a one-off read of the theme pref. */
val LocalThemePref = compositionLocalOf<ThemePref> { ThemePref.SYSTEM }

@Composable
fun rememberUserPrefs(): UserPrefs = androidx.compose.runtime.remember { UserPrefs() }
