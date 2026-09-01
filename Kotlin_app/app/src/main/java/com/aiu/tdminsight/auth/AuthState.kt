package com.aiu.tdminsight.auth

sealed class AuthState {
    object Loading       : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(
        val userId: String,
        val email: String,
        val sessionToken: String,
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
