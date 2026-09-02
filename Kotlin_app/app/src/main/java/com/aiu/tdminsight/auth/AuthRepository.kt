package com.aiu.tdminsight.auth

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * AuthRepository — the application-level authentication abstraction.
 * Screens and ViewModels use this; they never touch ClerkAuthManager directly.
 *
 * Session persistence: the session token and user metadata are stored in
 * private SharedPreferences so the user stays logged in across restarts.
 * Clerk tokens are short-lived JWTs; if a stored token is expired the user
 * will receive an auth error on the next Supabase call and be asked to re-sign-in.
 */
class AuthRepository(
    private val clerk: ClerkAuthManager,
    private val prefs: SharedPreferences,
) {
    companion object {
        private const val KEY_USER_ID    = "clerk_user_id"
        private const val KEY_EMAIL      = "clerk_email"
        private const val KEY_TOKEN      = "clerk_session_token"
        private const val KEY_SESSION_ID = "clerk_session_id"
    }

    // ── Persisted session (survives app restarts) ─────────────────────────

    fun savedSession(): AuthState.Authenticated? {
        val id    = prefs.getString(KEY_USER_ID, null)
        val email = prefs.getString(KEY_EMAIL,   null)
        val token = prefs.getString(KEY_TOKEN,   null)
        return if (id != null && email != null && token != null)
            AuthState.Authenticated(userId = id, email = email, sessionToken = token)
        else null
    }

    private fun saveSession(userId: String, email: String, token: String, sessionId: String = "") {
        prefs.edit {
            putString(KEY_USER_ID,    userId)
            putString(KEY_EMAIL,      email)
            putString(KEY_TOKEN,      token)
            putString(KEY_SESSION_ID, sessionId)
        }
    }

    fun clearSession() {
        prefs.edit { clear() }
    }

    val savedSessionId: String get() = prefs.getString(KEY_SESSION_ID, "") ?: ""

    // ── Auth operations ───────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String): AuthState {
        return when (val r = clerk.signIn(email.trim(), password)) {
            is ClerkResult.Success -> {
                saveSession(r.userId, r.email, r.sessionToken, r.sessionId)
                AuthState.Authenticated(r.userId, r.email, r.sessionToken, isNewUser = false)
            }
            is ClerkResult.Failure -> AuthState.Error(r.message)
        }
    }

    suspend fun signUp(email: String, password: String): AuthState {
        return when (val r = clerk.signUp(email.trim(), password)) {
            is ClerkResult.Success -> {
                saveSession(r.userId, r.email, r.sessionToken, r.sessionId)
                AuthState.Authenticated(r.userId, r.email, r.sessionToken, isNewUser = true)
            }
            is ClerkResult.Failure -> AuthState.Error(r.message)
        }
    }

    fun signOut(): AuthState {
        clearSession()
        return AuthState.Unauthenticated
    }

    /**
     * Returns an up-to-date Supabase JWT.
     * First tries to refresh via the Clerk "supabase" JWT template; falls back
     * to the stored session token (which is already a valid Clerk JWT).
     */
    suspend fun getSupabaseToken(): String? {
        val sessionId = savedSessionId
        val refreshed = if (sessionId.isNotBlank()) clerk.refreshSupabaseToken(sessionId) else null
        return refreshed ?: prefs.getString(KEY_TOKEN, null)
    }

    val isConfigured: Boolean get() = clerk.isConfigured
}
