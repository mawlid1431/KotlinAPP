package com.aiu.tdminsight.auth

sealed class AuthState {
    object Loading         : AuthState()
    object Unauthenticated : AuthState()

    /**
     * The signed-in Clerk user.
     *
     * Every field here comes from Clerk — nothing is defaulted to placeholder
     * data. [firstName], [lastName] and [imageUrl] are nullable because Clerk
     * only supplies them when the user has them set (Google sign-in normally
     * provides all three; email/password sign-up often provides none).
     */
    data class Authenticated(
        val userId: String,
        val email: String,
        val sessionToken: String,
        val isNewUser: Boolean = false,
        val firstName: String? = null,
        val lastName: String? = null,
        val imageUrl: String? = null,
    ) : AuthState() {

        /** "Ada Lovelace", or the email's local part when Clerk has no name. */
        val fullName: String
            get() = listOfNotNull(
                firstName?.takeIf { it.isNotBlank() },
                lastName?.takeIf { it.isNotBlank() },
            ).joinToString(" ").ifBlank { email.substringBefore('@') }

        /** Up to two letters for the avatar fallback, e.g. "AL". */
        val initials: String
            get() {
                val first = firstName?.trim()?.firstOrNull()
                val last  = lastName?.trim()?.firstOrNull()
                return when {
                    first != null && last != null -> "$first$last".uppercase()
                    first != null                 -> first.uppercase().toString()
                    else -> email.trimStart().firstOrNull()?.uppercase()?.toString() ?: "?"
                }
            }
    }

    /** Clerk emailed a 6-digit code; sign-up finishes once it is entered. */
    data class AwaitingEmailCode(
        val email: String,
        val message: String? = null,
        val busy: Boolean = false,
    ) : AuthState()

    data class Error(val message: String) : AuthState()
}
