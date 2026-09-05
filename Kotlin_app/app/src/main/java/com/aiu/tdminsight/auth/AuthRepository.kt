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
        private const val KEY_FIRST_NAME = "clerk_first_name"
        private const val KEY_LAST_NAME  = "clerk_last_name"
        private const val KEY_IMAGE_URL  = "clerk_image_url"
    }

    // ── Persisted session (survives app restarts) ─────────────────────────

    fun savedSession(): AuthState.Authenticated? {
        val id    = prefs.getString(KEY_USER_ID, null)
        val email = prefs.getString(KEY_EMAIL,   null)
        val token = prefs.getString(KEY_TOKEN,   null)
        return if (id != null && email != null && token != null)
            AuthState.Authenticated(
                userId       = id,
                email        = email,
                sessionToken = token,
                firstName    = prefs.getString(KEY_FIRST_NAME, null),
                lastName     = prefs.getString(KEY_LAST_NAME,  null),
                imageUrl     = prefs.getString(KEY_IMAGE_URL,  null),
            )
        else null
    }

    private fun saveSession(
        userId: String,
        email: String,
        token: String,
        sessionId: String = "",
        firstName: String? = null,
        lastName: String? = null,
        imageUrl: String? = null,
    ) {
        prefs.edit {
            putString(KEY_USER_ID,    userId)
            putString(KEY_EMAIL,      email)
            putString(KEY_TOKEN,      token)
            putString(KEY_SESSION_ID, sessionId)
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME,  lastName)
            putString(KEY_IMAGE_URL,  imageUrl)
        }
    }

    fun clearSession() {
        prefs.edit {
            remove(KEY_USER_ID)
            remove(KEY_EMAIL)
            remove(KEY_TOKEN)
            remove(KEY_SESSION_ID)
            remove(KEY_FIRST_NAME)
            remove(KEY_LAST_NAME)
            remove(KEY_IMAGE_URL)
        }
    }

    val savedSessionId: String get() = prefs.getString(KEY_SESSION_ID, "") ?: ""

    // ── Auth operations ───────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String): AuthState =
        toAuthState(clerk.signIn(email.trim(), password), isNewUserDefault = false)

    suspend fun signUp(email: String, password: String): AuthState =
        toAuthState(clerk.signUp(email.trim(), password), isNewUserDefault = true)

    /**
     * Step 1 of Google sign-in — returns the URL the caller must open in a browser.
     */
    suspend fun startGoogleOAuth(): ClerkOAuthStart = clerk.startGoogleOAuth()

    /**
     * Step 2 of Google sign-in — called once the browser redirects back into the app.
     */
    suspend fun completeGoogleOAuth(): AuthState =
        toAuthState(clerk.completeGoogleOAuth(), isNewUserDefault = false)

    private fun toAuthState(result: ClerkResult, isNewUserDefault: Boolean): AuthState =
        when (result) {
            is ClerkResult.Success -> {
                saveSession(
                    userId    = result.userId,
                    email     = result.email,
                    token     = result.sessionToken,
                    sessionId = result.sessionId,
                    firstName = result.firstName,
                    lastName  = result.lastName,
                    imageUrl  = result.imageUrl,
                )
                AuthState.Authenticated(
                    userId       = result.userId,
                    email        = result.email,
                    sessionToken = result.sessionToken,
                    isNewUser    = result.isNewUser || isNewUserDefault,
                    firstName    = result.firstName,
                    lastName     = result.lastName,
                    imageUrl     = result.imageUrl,
                )
            }
            is ClerkResult.Failure -> AuthState.Error(result.message)
        }

    /**
     * Re-reads the profile from Clerk and updates the persisted session.
     * Returns the refreshed state, or null when Clerk had nothing newer.
     */
    suspend fun refreshProfileFromClerk(): AuthState.Authenticated? {
        val current = savedSession() ?: return null
        val user = clerk.fetchCurrentUser() ?: return null
        val refreshed = current.copy(
            email     = user.primaryEmail() ?: current.email,
            firstName = user.firstName ?: current.firstName,
            lastName  = user.lastName  ?: current.lastName,
            imageUrl  = user.imageUrl?.takeIf { it.isNotBlank() } ?: current.imageUrl,
        )
        saveSession(
            userId    = refreshed.userId,
            email     = refreshed.email,
            token     = refreshed.sessionToken,
            sessionId = savedSessionId,
            firstName = refreshed.firstName,
            lastName  = refreshed.lastName,
            imageUrl  = refreshed.imageUrl,
        )
        return refreshed
    }

    /** Deletes the signed-in user's own Clerk account (no secret key involved). */
    suspend fun deleteClerkAccount(): ClerkDeleteResult = clerk.deleteOwnAccount()

    /**
     * Confirms a restored session is still valid with Clerk.
     * null means "could not check" — the caller keeps the local session.
     */
    suspend fun isSessionStillValid(): Boolean? = clerk.hasValidSession()

    fun signOut(): AuthState {
        clearSession()
        // Drop the Clerk device token too, so the next sign-in starts a fresh
        // Clerk client instead of resuming this one.
        clerk.clearDeviceToken()
        return AuthState.Unauthenticated
    }

    /**
     * Returns an up-to-date Supabase JWT.
     * First tries to refresh via the Clerk "supabase" JWT template; falls back
     * to the stored session token (which is already a valid Clerk JWT).
     *
     * NOTE: deliberately not called yet. SupabaseRepository currently talks to
     * PostgREST with the anon key and scopes rows by filtering on user_id in the
     * query (see supabase/update_v2.sql, "Option B"). This function is the hook
     * for switching to real per-user RLS: create the "supabase" JWT template in
     * the Clerk Dashboard, pass this token as the Supabase access token, then
     * enable the per-user policies in update_v2.sql "Option A". It is kept
     * rather than deleted because removing it would erase the only bridge
     * between Clerk identity and Supabase row security.
     */
    suspend fun getSupabaseToken(): String? {
        val sessionId = savedSessionId
        val refreshed = if (sessionId.isNotBlank()) clerk.refreshSupabaseToken(sessionId) else null
        return refreshed ?: prefs.getString(KEY_TOKEN, null)
    }

    val isConfigured: Boolean get() = clerk.isConfigured
}
