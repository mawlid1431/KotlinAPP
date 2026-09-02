package com.aiu.tdminsight.auth

sealed class AuthState {
    object Loading       : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(
        val userId: String,
        val email: String,
        val sessionToken: String,
        val isNewUser: Boolean = false,
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
