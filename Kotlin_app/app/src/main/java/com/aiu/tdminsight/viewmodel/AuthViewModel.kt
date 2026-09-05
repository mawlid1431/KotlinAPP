package com.aiu.tdminsight.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiu.tdminsight.TdmApplication
import com.aiu.tdminsight.auth.AuthState
import com.aiu.tdminsight.auth.ClerkDeleteResult
import com.aiu.tdminsight.auth.ClerkOAuthStart
import com.aiu.tdminsight.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel — single source of truth for who is signed in.
 *
 * IMPORTANT: this must be one instance per Activity, not one per navigation
 * destination. MainActivity owns it and passes it down; screens inside the
 * NavHost must never call `viewModel()` for it, because inside a NavHost that
 * resolves to the NavBackStackEntry and silently creates a *second* instance
 * whose sign-out the app shell never observes.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = (application as TdmApplication).authRepository
    private val supabase = (application as TdmApplication).supabaseRepository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // True only when the user just signed in or signed up this session.
    // Session restores do NOT set this, so the welcome screen only shows
    // once per fresh login, not on every app open.
    private val _freshLogin = MutableStateFlow(false)
    val freshLogin: StateFlow<Boolean> = _freshLogin.asStateFlow()

    // Set when Clerk hands back a Google consent URL that the Activity must
    // open in a browser. The Activity clears it via consumeOAuthUrl().
    private val _pendingOAuthUrl = MutableStateFlow<String?>(null)
    val pendingOAuthUrl: StateFlow<String?> = _pendingOAuthUrl.asStateFlow()

    // The user's row in Supabase (institution / department / role live here).
    // Null until the first successful load.
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _accountDeletion = MutableStateFlow<AccountDeletionState>(AccountDeletionState.Idle)
    val accountDeletion: StateFlow<AccountDeletionState> = _accountDeletion.asStateFlow()

    val isConfigured: Boolean get() = authRepo.isConfigured

    init {
        val saved = authRepo.savedSession()
        // Restore optimistically so a returning user never sees a login flash,
        // then confirm with Clerk in the background.
        _authState.value = saved ?: AuthState.Unauthenticated
        // Session was restored from prefs — not a fresh login, no welcome screen.

        if (saved != null) {
            viewModelScope.launch {
                // Only a definitive `false` signs the user out. `null` means we
                // could not reach Clerk (offline), so the local session stands.
                if (authRepo.isSessionStillValid() == false) {
                    _authState.value = authRepo.signOut()
                } else {
                    // Session is good: make sure Supabase reflects the current
                    // Clerk identity, and load this user's profile.
                    syncUserToSupabase(saved)
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepo.signIn(email, password)
            _authState.value = result
            if (result is AuthState.Authenticated) {
                _freshLogin.value = true
                syncUserToSupabase(result)
            }
        }
    }

    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepo.signUp(email, password)
            _authState.value = result
            if (result is AuthState.Authenticated) {
                _freshLogin.value = true
                syncUserToSupabase(result)
            }
        }
    }

    /**
     * Confirms the 6-digit code Clerk emailed during sign-up. Only on success
     * does the Clerk user actually exist, so the Supabase sync happens here too.
     */
    fun verifyEmailCode(code: String) {
        val pending = _authState.value as? AuthState.AwaitingEmailCode ?: return
        _authState.value = pending.copy(message = null, busy = true)
        viewModelScope.launch {
            val result = authRepo.verifyEmailCode(code, pending.email)
            _authState.value = when (result) {
                // Keep the user on the code screen when the code was wrong.
                is AuthState.Error -> pending.copy(message = result.message, busy = false)
                else -> result
            }
            if (result is AuthState.Authenticated) {
                _freshLogin.value = true
                syncUserToSupabase(result)
            }
        }
    }

    /** Asks Clerk to email the sign-up code again. */
    fun resendEmailCode() {
        val pending = _authState.value as? AuthState.AwaitingEmailCode ?: return
        viewModelScope.launch {
            val error = authRepo.resendEmailCode()
            _authState.value = AuthState.AwaitingEmailCode(
                pending.email,
                error ?: "A new code is on its way to ${pending.email}.",
            )
        }
    }

    /** Abandons a pending sign-up and returns to the sign-in screen. */
    fun cancelEmailVerification() {
        if (_authState.value is AuthState.AwaitingEmailCode) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // -- Clerk identity -> Supabase ----------------------------------------

    /**
     * Pushes the Clerk identity into `user_profiles`, then reads the row back
     * so the UI also has the application-owned fields (institution, role...).
     *
     * The write is an upsert keyed on the Clerk user ID, so signing in twice
     * updates one row instead of creating a second user. A failure here is
     * logged and swallowed: losing the network must not block the user from
     * reaching the app they just authenticated into.
     */
    private suspend fun syncUserToSupabase(auth: AuthState.Authenticated) {
        supabase.syncUserProfile(
            userId      = auth.userId,
            email       = auth.email,
            firstName   = auth.firstName,
            lastName    = auth.lastName,
            displayName = auth.fullName,
            avatarUrl   = auth.imageUrl,
        )
        _profile.value = supabase.loadUserProfile(auth.userId)
    }

    /** Re-reads the profile from Clerk and Supabase (used by the Profile screen). */
    fun refreshProfile() {
        val current = _authState.value as? AuthState.Authenticated ?: return
        viewModelScope.launch {
            val refreshed = authRepo.refreshProfileFromClerk()
            if (refreshed != null) _authState.value = refreshed
            syncUserToSupabase(refreshed ?: current)
        }
    }

    // ── Google sign-in ────────────────────────────────────────────────────

    /** Step 1 — ask Clerk for the Google consent URL. */
    fun startGoogleSignIn() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            when (val start = authRepo.startGoogleOAuth()) {
                is ClerkOAuthStart.Success -> {
                    // Back to Unauthenticated so the login screen renders normally
                    // behind the browser. If the user backs out of Google without
                    // finishing, they land on a usable screen instead of a spinner.
                    _authState.value = AuthState.Unauthenticated
                    _pendingOAuthUrl.value = start.url
                }
                is ClerkOAuthStart.Failure -> _authState.value = AuthState.Error(start.message)
            }
        }
    }

    /** Called by MainActivity once the browser intent has been fired. */
    fun consumeOAuthUrl() {
        _pendingOAuthUrl.value = null
    }

    /**
     * Step 2 - called when the browser redirects back into the app.
     *
     * [callbackUrl] is the full deep link. It matters: Clerk rotates the device
     * token across the browser trip and returns the new one on this URL.
     */
    fun completeGoogleSignIn(callbackUrl: String? = null) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepo.completeGoogleOAuth(callbackUrl)
            _authState.value = result
            if (result is AuthState.Authenticated) {
                _freshLogin.value = true
                syncUserToSupabase(result)
            }
        }
    }

    fun signOut() {
        _freshLogin.value = false
        _pendingOAuthUrl.value = null
        // Drop the previous user's profile so the next person to sign in can
        // never briefly see it.
        _profile.value = null
        _accountDeletion.value = AccountDeletionState.Idle
        _authState.value = authRepo.signOut()
    }

    // -- Delete account ----------------------------------------------------

    /**
     * Deletes this user's Supabase rows, then their Clerk account, then ends
     * the session. Ordering matters: the Supabase rows are keyed by the Clerk
     * user ID, so they must go first while that ID is still known.
     */
    fun deleteAccount() {
        val auth = _authState.value as? AuthState.Authenticated ?: return
        _accountDeletion.value = AccountDeletionState.InProgress
        viewModelScope.launch {
            val dataDeleted = supabase.deleteAllUserData(auth.userId)
            when (val clerkResult = authRepo.deleteClerkAccount()) {
                is ClerkDeleteResult.Success -> {
                    _accountDeletion.value = AccountDeletionState.Idle
                    signOut()
                }
                is ClerkDeleteResult.Failure -> {
                    // The Supabase rows may already be gone; say so plainly
                    // rather than implying the account is fully intact.
                    _accountDeletion.value = AccountDeletionState.Failed(
                        if (dataDeleted)
                            "Your saved cases were removed, but the Clerk account could not be deleted: " + clerkResult.message
                        else
                            clerkResult.message
                    )
                }
            }
        }
    }

    fun dismissDeletionError() {
        _accountDeletion.value = AccountDeletionState.Idle
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Called by WelcomeScreen's "Continue" button so the screen never re-appears.
    fun consumeWelcome() {
        _freshLogin.value = false
    }
}

/** UI state for the "Delete account" flow. */
sealed class AccountDeletionState {
    object Idle       : AccountDeletionState()
    object InProgress : AccountDeletionState()
    data class Failed(val message: String) : AccountDeletionState()
}
