package com.aiu.tdminsight.auth

import android.util.Base64
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * ClerkAuthManager — calls Clerk's Frontend API directly via Ktor HTTP.
 *
 * Setup required in the Clerk Dashboard (https://dashboard.clerk.com):
 *   1. Enable "Email + Password" authentication.
 *   2. Create a JWT template named "supabase" with the following claims so
 *      Supabase RLS can identify users (Settings → JWT Templates → New template):
 *        { "role": "authenticated", "email": "{{user.primary_email_address}}" }
 *      Set the "Lifetime" to your preferred value (e.g. 3600 s).
 *   3. Copy the publishable key (pk_test_/pk_live_) into local.properties.
 *
 * The publishable key encodes the Frontend API domain in base64 after the
 * "pk_test_" / "pk_live_" prefix:  base64Decode(suffix) → "domain.clerk.dev$"
 */
class ClerkAuthManager(private val publishableKey: String) {

    private val frontendApiUrl: String = deriveFrontendApiUrl(publishableKey)

    private val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    val isConfigured: Boolean get() = publishableKey.isNotBlank()

    // ── Public API ────────────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String): ClerkResult {
        if (!isConfigured) return ClerkResult.Failure("Clerk not configured — add CLERK_PUBLISHABLE_KEY to local.properties.")
        return try {
            val response: ClerkClientResponse = http.submitForm(
                url = "$frontendApiUrl/v1/client/sign_ins",
                formParameters = parameters {
                    append("strategy", "password")
                    append("identifier", email)
                    append("password", password)
                }
            ) {
                header("x-publishable-key", publishableKey)
            }.body()

            val signIn = response.response ?: return ClerkResult.Failure("Unexpected Clerk response.")
            when (signIn.status) {
                "complete" -> {
                    val session = response.client?.sessions?.firstOrNull()
                    val jwt       = session?.lastActiveToken?.jwt ?: ""
                    val userId    = session?.user?.id ?: signIn.createdSessionId ?: ""
                    val sessionId = session?.id ?: signIn.createdSessionId ?: ""
                    val emailVal  = session?.user?.emailAddresses?.firstOrNull()?.emailAddress ?: email
                    ClerkResult.Success(userId = userId, email = emailVal, sessionToken = jwt, sessionId = sessionId)
                }
                else -> ClerkResult.Failure("Sign-in incomplete (status: ${signIn.status}).")
            }
        } catch (e: Exception) {
            ClerkResult.Failure("Sign-in failed: ${e.message}")
        }
    }

    suspend fun signUp(email: String, password: String): ClerkResult {
        if (!isConfigured) return ClerkResult.Failure("Clerk not configured — add CLERK_PUBLISHABLE_KEY to local.properties.")
        return try {
            val response: ClerkClientResponse = http.submitForm(
                url = "$frontendApiUrl/v1/client/sign_ups",
                formParameters = parameters {
                    append("email_address", email)
                    append("password", password)
                }
            ) {
                header("x-publishable-key", publishableKey)
            }.body()

            val signUp = response.response
            when {
                signUp == null -> ClerkResult.Failure("Unexpected Clerk response.")
                signUp.status == "complete" -> {
                    val session   = response.client?.sessions?.firstOrNull()
                    val jwt       = session?.lastActiveToken?.jwt ?: ""
                    val userId    = session?.user?.id ?: ""
                    val sessionId = session?.id ?: ""
                    ClerkResult.Success(userId = userId, email = email, sessionToken = jwt, sessionId = sessionId)
                }
                signUp.status == "missing_requirements" ->
                    // Email verification may be required — inform the user
                    ClerkResult.Failure("Email verification required. Check your inbox.")
                else -> ClerkResult.Failure("Sign-up incomplete (status: ${signUp.status}).")
            }
        } catch (e: Exception) {
            ClerkResult.Failure("Sign-up failed: ${e.message}")
        }
    }

    /**
     * Fetches a fresh session JWT using the "supabase" token template.
     * Call this before making authenticated Supabase requests if the token may be stale.
     *
     * Requires a JWT template named "supabase" in the Clerk Dashboard.
     */
    suspend fun refreshSupabaseToken(sessionId: String): String? {
        if (!isConfigured || sessionId.isBlank()) return null
        return try {
            val response: ClerkTokenResponse = http.post(
                "$frontendApiUrl/v1/client/sessions/$sessionId/tokens/supabase"
            ) {
                header("x-publishable-key", publishableKey)
            }.body()
            response.jwt
        } catch (e: Exception) {
            null
        }
    }

    fun close() = http.close()

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun deriveFrontendApiUrl(key: String): String {
        return try {
            val base64Part = key
                .removePrefix("pk_live_")
                .removePrefix("pk_test_")
            val decoded = String(Base64.decode(base64Part, Base64.DEFAULT)).trimEnd('$', '\n')
            "https://$decoded"
        } catch (e: Exception) {
            ""
        }
    }

    // ── Wire-format models (Clerk Frontend API v1 responses) ─────────────

    @Serializable
    data class ClerkClientResponse(
        val response: ClerkSignResponse? = null,
        val client: ClerkClientData? = null,
    )

    @Serializable
    data class ClerkSignResponse(
        val id: String? = null,
        val status: String? = null,
        @SerialName("created_session_id") val createdSessionId: String? = null,
    )

    @Serializable
    data class ClerkClientData(
        val sessions: List<ClerkSession>? = null,
    )

    @Serializable
    data class ClerkSession(
        val id: String? = null,
        val user: ClerkUser? = null,
        @SerialName("last_active_token") val lastActiveToken: ClerkToken? = null,
    )

    @Serializable
    data class ClerkUser(
        val id: String? = null,
        @SerialName("email_addresses") val emailAddresses: List<ClerkEmail>? = null,
    )

    @Serializable
    data class ClerkEmail(
        @SerialName("email_address") val emailAddress: String? = null,
    )

    @Serializable
    data class ClerkToken(val jwt: String? = null)

    @Serializable
    data class ClerkTokenResponse(val jwt: String? = null)
}

sealed class ClerkResult {
    data class Success(
        val userId: String,
        val email: String,
        val sessionToken: String,
        val sessionId: String = "",
    ) : ClerkResult()
    data class Failure(val message: String) : ClerkResult()
}
