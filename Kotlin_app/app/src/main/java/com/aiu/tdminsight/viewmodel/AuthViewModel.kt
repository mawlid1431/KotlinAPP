package com.aiu.tdminsight.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiu.tdminsight.TdmApplication
import com.aiu.tdminsight.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = (application as TdmApplication).authRepository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // True only when the user just signed in or signed up this session.
    // Session restores do NOT set this, so the welcome screen only shows
    // once per fresh login, not on every app open.
    private val _freshLogin = MutableStateFlow(false)
    val freshLogin: StateFlow<Boolean> = _freshLogin.asStateFlow()

    val isConfigured: Boolean get() = authRepo.isConfigured

    init {
        val saved = authRepo.savedSession()
        _authState.value = saved ?: AuthState.Unauthenticated
        // Session was restored from prefs — not a fresh login, no welcome screen.
    }

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepo.signIn(email, password)
            _authState.value = result
            if (result is AuthState.Authenticated) _freshLogin.value = true
        }
    }

    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepo.signUp(email, password)
            _authState.value = result
            if (result is AuthState.Authenticated) _freshLogin.value = true
        }
    }

    fun signOut() {
        _freshLogin.value = false
        _authState.value = authRepo.signOut()
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
