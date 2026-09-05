package com.aiu.tdminsight.auth

import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * ClerkAuthManager — calls Clerk's Frontend API (FAPI) directly via Ktor HTTP.
 *
 * Setup required in the Clerk Dashboard (https://dashboard.clerk.com):
 *   1. Enable "Email + Password" authentication.
 *   2. Enable the Google social connection (User & Authentication -> Social
 *      connections -> Google). Without this, "Continue with Google" comes back
 *      with a Clerk error saying the strategy is not enabled.
 *   3. Optionally create a JWT template named "supabase" so Supabase RLS can
 *      identify users:
 *        { "role": "authenticated", "email": "{{user.primary_email_address}}" }
 *   4. Copy the publishable key (pk_test_/pk_live_) into local.properties.
 *
 * The publishable key encodes the Frontend API domain in base64 after the
 * "pk_test_" / "pk_live_" prefix:  base64Decode(suffix) -> "domain.clerk.dev$"
 *
 * NATIVE CLIENTS: FAPI normally tracks the Clerk "client" (device) with a cookie.
 * There is no cookie jar here, so every request carries `_is_native=1` and Clerk
 * returns a device token in the `Authorization` response header instead. That
 * token is persisted and replayed on later requests, which is what lets the
 * Google OAuth browser round-trip resolve back to the same Clerk client.
 */
class ClerkAuthManager(
    private val publishableKey: String,
    private val prefs: SharedPreferences? = null,
) {

    companion object {
        private const val TAG = "ClerkAuthManager"
        private const val KEY_DEVICE_TOKEN = "clerk_device_token"

        /** Parameter names Clerk has used for the rotated device token. */
        private val TOKEN_PARAMS = listOf(
            "__clerk_db_jwt",
            "__dev_session",
            "__session",
            "__clerk_handshake",
        )

        /** Must match the intent-filter registered for MainActivity in AndroidManifest.xml. */
        const val OAUTH_REDIRECT_URL = "tdminsight://oauth-callback"
    }

    private val frontendApiUrl: String = deriveFrontendApiUrl(publishableKey)

    init {
        android.util.Log.d(TAG, "derivedUrl='$frontendApiUrl'")
    }

    private val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    val isConfigured: Boolean get() = publishableKey.isNotBlank() && frontendApiUrl.isNotBlank()

    // ── Device (client) token — persisted so the OAuth round-trip resolves ────

    private var deviceTokenCache: String? = null

    private var deviceToken: String?
        get() = deviceTokenCache
            ?: prefs?.getString(KEY_DEVICE_TOKEN, null)?.also { deviceTokenCache = it }
        set(value) {
            deviceTokenCache = value
            prefs?.edit { putString(KEY_DEVICE_TOKEN, value) }
        }

    /** Called on sign-out so the next sign-in starts from a clean Clerk client. */
    fun clearDeviceToken() {
        deviceTokenCache = null
        prefs?.edit { remove(KEY_DEVICE_TOKEN) }
    }

    private fun HttpRequestBuilder.clerkHeaders() {
        header("x-publishable-key", publishableKey)
        deviceToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    /** Clerk returns a refreshed device token on native responses; keep the latest. */
    private fun captureDeviceToken(response: HttpResponse) {
        response.headers[HttpHeaders.Authorization]
            ?.removePrefix("Bearer ")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { deviceToken = it }
    }

    private fun endpoint(path: String) = "$frontendApiUrl$path?_is_native=1"

    private fun notConfigured() =
        ClerkResult.Failure("Clerk not configured — add CLERK_PUBLISHABLE_KEY to local.properties.")

    // ── Email + password ──────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String): ClerkResult {
        if (!isConfigured) return notConfigured()
        return try {
            val httpResponse = http.submitForm(
                url = endpoint("/v1/client/sign_ins"),
                formParameters = parameters {
                    append("strategy", "password")
                    append("identifier", email)
                    append("password", password)
                }
            ) { clerkHeaders() }
            captureDeviceToken(httpResponse)

            val response: ClerkClientResponse = httpResponse.body()
            val signIn = response.response ?: return ClerkResult.Failure("Unexpected Clerk response.")
            when (signIn.status) {
                "complete" -> {
                    val session = response.client?.sessions?.firstOrNull()
                    toSuccess(session, fallbackEmail = email, isNewUser = false)
                        ?: ClerkResult.Failure("Signed in, but Clerk returned no session.")
                }
                else -> ClerkResult.Failure("Sign-in incomplete (status: ${signIn.status}).")
            }
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            ClerkResult.Failure(errorMessage(e, "Invalid email or password."))
        } catch (e: Exception) {
            ClerkResult.Failure("Sign-in failed: ${e.message}")
        }
    }

    suspend fun signUp(email: String, password: String): ClerkResult {
        if (!isConfigured) return notConfigured()
        return try {
            val httpResponse = http.submitForm(
                url = endpoint("/v1/client/sign_ups"),
                formParameters = parameters {
                    append("email_address", email)
                    append("password", password)
                }
            ) { clerkHeaders() }
            captureDeviceToken(httpResponse)

            val response: ClerkClientResponse = httpResponse.body()
            val signUp = response.response
            when {
                signUp == null -> ClerkResult.Failure("Unexpected Clerk response.")
                signUp.status == "complete" -> {
                    val session = response.client?.sessions?.firstOrNull()
                    toSuccess(session, fallbackEmail = email, isNewUser = true)
                        ?: ClerkResult.Failure("Account created, but Clerk returned no session.")
                }
                signUp.status == "missing_requirements" ->
                    ClerkResult.Failure("Email verification required. Check your inbox.")
                else -> ClerkResult.Failure("Sign-up incomplete (status: ${signUp.status}).")
            }
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            ClerkResult.Failure(errorMessage(e, "Sign-up failed. Check your email and password requirements."))
        } catch (e: Exception) {
            ClerkResult.Failure("Sign-up failed: ${e.message}")
        }
    }

    // ── Google OAuth (two-step: open browser, then resolve on deep-link) ─────

    /**
     * Step 1 — ask Clerk to begin an `oauth_google` sign-in attempt.
     *
     * Returns the Google consent URL that the caller must open in a browser.
     * Clerk sends the browser back to [OAUTH_REDIRECT_URL] when the user is done.
     */
    suspend fun startGoogleOAuth(): ClerkOAuthStart {
        if (!isConfigured) {
            return ClerkOAuthStart.Failure("Clerk not configured — add CLERK_PUBLISHABLE_KEY to local.properties.")
        }
        return try {
            // Start from a clean client so a previous half-finished attempt
            // cannot block this one.
            clearDeviceToken()

            val httpResponse = http.submitForm(
                url = endpoint("/v1/client/sign_ins"),
                formParameters = parameters {
                    append("strategy", "oauth_google")
                    append("redirect_url", OAUTH_REDIRECT_URL)
                }
            ) { clerkHeaders() }
            captureDeviceToken(httpResponse)

            val response: ClerkClientResponse = httpResponse.body()
            val redirect = response.response?.firstFactorVerification?.externalVerificationRedirectUrl
                ?: response.response?.verification?.externalVerificationRedirectUrl

            if (redirect.isNullOrBlank()) {
                ClerkOAuthStart.Failure(
                    "Clerk did not return a Google sign-in URL. Enable the Google " +
                        "social connection in the Clerk Dashboard."
                )
            } else {
                ClerkOAuthStart.Success(redirect)
            }
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            ClerkOAuthStart.Failure(
                errorMessage(e, "Google sign-in is not enabled for this Clerk application.")
            )
        } catch (e: Exception) {
            ClerkOAuthStart.Failure("Could not start Google sign-in: ${e.message}")
        }
    }

    /**
     * Step 2 — called after the browser returns to [OAUTH_REDIRECT_URL].
     *
     * Reads the Clerk client for this device. If Google returned an account that
     * has never signed up before, Clerk marks the attempt "transferable" and the
     * app must convert it into a sign-up before a session exists.
     */
    suspend fun completeGoogleOAuth(callbackUrl: String? = null): ClerkResult {
        if (!isConfigured) return notConfigured()
        return try {
            // Clerk ROTATES the device token across the browser round-trip and
            // hands the new one back on the redirect URL. Without adopting it,
            // GET /v1/client below is asking about the OLD client - the one that
            // never completed the OAuth - so it legitimately reports no session.
            adoptDeviceTokenFrom(callbackUrl)

            val client = fetchClient()
                ?: return ClerkResult.Failure("Could not read the Clerk session after Google sign-in.")

            client.sessions?.firstOrNull()?.let { session ->
                return toSuccess(session, fallbackEmail = "", isNewUser = false)
                    ?: ClerkResult.Failure("Google sign-in returned an unusable session.")
            }

            // New Google user -> Clerk wants the OAuth attempt converted into a
            // sign-up. IMPORTANT: Clerk reports this on the VERIFICATION, not on
            // the sign-in itself. A first-time Google user looks like:
            //     sign_in.status                            = "needs_identifier"
            //     sign_in.first_factor_verification.status  = "transferable"
            //     ...error.code                             = "external_account_not_found"
            // Checking only sign_in.status therefore misses every new user and
            // reports "sign-in did not complete" even though Google succeeded.
            val verification = client.signIn?.firstFactorVerification
            val transferable = verification?.status == "transferable" ||
                client.signIn?.status == "transferable" ||
                client.signUp?.status == "missing_requirements"
            if (transferable) {
                val httpResponse = http.submitForm(
                    url = endpoint("/v1/client/sign_ups"),
                    formParameters = parameters { append("transfer", "true") }
                ) { clerkHeaders() }
                captureDeviceToken(httpResponse)

                val response: ClerkClientResponse = httpResponse.body()
                val session = response.client?.sessions?.firstOrNull()
                    ?: fetchClient()?.sessions?.firstOrNull()
                return toSuccess(session, fallbackEmail = "", isNewUser = true)
                    ?: ClerkResult.Failure(
                        "Google sign-up did not complete (transfer status: " +
                            "${response.response?.status}). Please try again."
                    )
            }

            // Surface what Clerk actually said rather than a generic message,
            // so a failure here is diagnosable from the screen.
            val clerkReason = verification?.error?.longMessage
                ?: verification?.error?.message
            val statusInfo = "sign_in=${client.signIn?.status}, verification=${verification?.status}"
            android.util.Log.w(TAG, "Google sign-in incomplete: $statusInfo, error=$clerkReason")
            ClerkResult.Failure(
                clerkReason?.let { "Google sign-in did not complete: $it" }
                    ?: "Google sign-in did not complete. Please try again."
            )
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            ClerkResult.Failure(errorMessage(e, "Google sign-in failed."))
        } catch (e: Exception) {
            ClerkResult.Failure("Google sign-in failed: ${e.message}")
        }
    }

    /**
     * Is the persisted session still real, according to Clerk?
     *
     * Returns true/false when Clerk gives a definitive answer, and null when we
     * simply could not reach Clerk (offline, timeout). Callers must treat null
     * as "keep the local session" so a flaky network never signs the user out.
     */
    suspend fun hasValidSession(): Boolean? {
        if (!isConfigured) return null
        // No device token means this install has no Clerk client at all, so any
        // locally stored session is a leftover and cannot be revalidated.
        if (deviceToken == null) return false
        return try {
            fetchClient()?.sessions?.any { it.status == "active" } == true
        } catch (e: Exception) {
            android.util.Log.w(TAG, "hasValidSession undetermined: ${e.message}")
            null
        }
    }

    /**
     * Re-reads the signed-in user straight from Clerk (GET /v1/me).
     *
     * The session payload already carries the profile, so this is only needed
     * to pick up changes the user made in Clerk since signing in. Returns null
     * when there is no session or Clerk is unreachable.
     */
    suspend fun fetchCurrentUser(): ClerkUser? {
        if (!isConfigured || deviceToken == null) return null
        return try {
            val httpResponse = http.get(endpoint("/v1/me")) { clerkHeaders() }
            captureDeviceToken(httpResponse)
            httpResponse.body<ClerkMeEnvelope>().response
        } catch (e: Exception) {
            android.util.Log.w(TAG, "fetchCurrentUser failed: ${e.message}")
            null
        }
    }

    /**
     * Permanently deletes the signed-in user's own Clerk account (DELETE /v1/me).
     *
     * This is the self-service deletion endpoint on the FRONTEND API: it acts
     * on whoever the device token identifies and needs no secret key, so no
     * privileged credential ever enters the app. It requires "Allow users to
     * delete their account" to be enabled in the Clerk Dashboard
     * (User & Authentication -> Restrictions); this instance has it enabled.
     */
    suspend fun deleteOwnAccount(): ClerkDeleteResult {
        if (!isConfigured) return ClerkDeleteResult.Failure("Clerk not configured.")
        if (deviceToken == null) return ClerkDeleteResult.Failure("No active Clerk session.")
        return try {
            val httpResponse = http.delete(endpoint("/v1/me")) { clerkHeaders() }
            if (httpResponse.status.isSuccess()) {
                clearDeviceToken()
                ClerkDeleteResult.Success
            } else {
                ClerkDeleteResult.Failure("Clerk refused the deletion (HTTP ${httpResponse.status.value}).")
            }
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            ClerkDeleteResult.Failure(
                errorMessage(e, "Could not delete the Clerk account. Account deletion may be disabled.")
            )
        } catch (e: Exception) {
            ClerkDeleteResult.Failure("Could not delete the Clerk account: ${e.message}")
        }
    }

    /**
     * Adopts the rotated device token that Clerk appends to the OAuth redirect.
     *
     * Clerk names this parameter differently depending on instance type, so all
     * the known spellings are checked. If none is present the existing token is
     * kept, which is the correct behaviour for flows that do not rotate it.
     */
    private fun adoptDeviceTokenFrom(callbackUrl: String?) {
        if (callbackUrl.isNullOrBlank()) return
        android.util.Log.d(TAG, "OAuth callback: $callbackUrl")
        val params = parseQueryParams(callbackUrl)
        val rotated = TOKEN_PARAMS.firstNotNullOfOrNull { name ->
            params[name]?.takeIf { it.isNotBlank() }
        }
        if (rotated != null) {
            android.util.Log.d(TAG, "Adopted rotated device token from callback")
            deviceToken = rotated
        } else {
            android.util.Log.w(
                TAG,
                "Callback carried no device token (params: ${params.keys}). " +
                    "Falling back to the token stored before the browser opened."
            )
        }
    }

    /** Reads both `?a=b` and `#a=b` pairs; Clerk has used each at times. */
    private fun parseQueryParams(url: String): Map<String, String> {
        val out = mutableMapOf<String, String>()
        val afterScheme = url.substringAfter("://", url)
        listOf(
            afterScheme.substringAfter('?', "").substringBefore('#'),
            afterScheme.substringAfter('#', ""),
        ).forEach { segment ->
            segment.split('&').forEach { pair ->
                if (pair.isBlank()) return@forEach
                val key = pair.substringBefore('=')
                val raw = pair.substringAfter('=', "")
                if (key.isNotBlank()) {
                    out[key] = runCatching {
                        java.net.URLDecoder.decode(raw, "UTF-8")
                    }.getOrDefault(raw)
                }
            }
        }
        return out
    }

    private suspend fun fetchClient(): ClerkClientData? {
        val httpResponse = http.get(endpoint("/v1/client")) { clerkHeaders() }
        captureDeviceToken(httpResponse)
        return httpResponse.body<ClerkClientEnvelope>().response
    }

    /**
     * Fetches a fresh session JWT using the "supabase" token template.
     * Requires a JWT template named "supabase" in the Clerk Dashboard.
     */
    suspend fun refreshSupabaseToken(sessionId: String): String? {
        if (!isConfigured || sessionId.isBlank()) return null
        return try {
            val httpResponse = http.post(
                endpoint("/v1/client/sessions/$sessionId/tokens/supabase")
            ) { clerkHeaders() }
            captureDeviceToken(httpResponse)
            httpResponse.body<ClerkTokenResponse>().jwt
        } catch (e: Exception) {
            android.util.Log.w(TAG, "refreshSupabaseToken failed: ${e.message}")
            null
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun toSuccess(
        session: ClerkSession?,
        fallbackEmail: String,
        isNewUser: Boolean,
    ): ClerkResult.Success? {
        if (session == null) return null
        val user   = session.user ?: return null
        val userId = user.id ?: return null
        return ClerkResult.Success(
            userId       = userId,
            email        = user.primaryEmail() ?: fallbackEmail.ifBlank { "signed-in user" },
            sessionToken = session.lastActiveToken?.jwt ?: "",
            sessionId    = session.id ?: "",
            isNewUser    = isNewUser,
            firstName    = user.firstName,
            lastName     = user.lastName,
            imageUrl     = user.imageUrl?.takeIf { it.isNotBlank() },
        )
    }

    private suspend fun errorMessage(
        e: io.ktor.client.plugins.ClientRequestException,
        fallback: String,
    ): String = try {
        val payload = e.response.body<ClerkErrorResponse>()
        payload.errors?.firstOrNull()?.longMessage
            ?: payload.errors?.firstOrNull()?.message
            ?: fallback
    } catch (_: Exception) {
        fallback
    }

    private fun deriveFrontendApiUrl(key: String): String {
        if (key.isBlank()) return ""
        return try {
            var base64 = key
                .removePrefix("pk_live_")
                .removePrefix("pk_test_")
                .trimEnd('$', '\n', '\r', ' ')

            base64 = base64.replace('-', '+').replace('_', '/')
            val pad = (4 - (base64.length % 4)) % 4
            base64 += "=".repeat(pad)

            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            val decoded = String(decodedBytes).trimEnd('$', '\n', '\r', ' ')
            if (decoded.isNotBlank()) "https://$decoded" else ""
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to derive URL from publishable key: ${e.message}", e)
            ""
        }
    }

    // ── Wire-format models (Clerk Frontend API v1 responses) ─────────────

    @Serializable
    data class ClerkErrorResponse(
        val errors: List<ClerkErrorItem>? = null,
    )

    @Serializable
    data class ClerkErrorItem(
        val message: String? = null,
        @SerialName("long_message") val longMessage: String? = null,
        val code: String? = null,
    )

    /** Shape of POST /v1/client/sign_ins and /v1/client/sign_ups. */
    @Serializable
    data class ClerkClientResponse(
        val response: ClerkSignResponse? = null,
        val client: ClerkClientData? = null,
    )

    /** Shape of GET /v1/client — here `response` IS the client object. */
    @Serializable
    data class ClerkClientEnvelope(
        val response: ClerkClientData? = null,
    )

    @Serializable
    data class ClerkSignResponse(
        val id: String? = null,
        val status: String? = null,
        @SerialName("created_session_id") val createdSessionId: String? = null,
        @SerialName("first_factor_verification") val firstFactorVerification: ClerkVerification? = null,
        val verification: ClerkVerification? = null,
    )

    @Serializable
    data class ClerkVerification(
        val status: String? = null,
        val strategy: String? = null,
        val error: ClerkErrorItem? = null,
        @SerialName("external_verification_redirect_url")
        val externalVerificationRedirectUrl: String? = null,
    )

    @Serializable
    data class ClerkClientData(
        val sessions: List<ClerkSession>? = null,
        @SerialName("sign_in") val signIn: ClerkSignResponse? = null,
        @SerialName("sign_up") val signUp: ClerkSignResponse? = null,
    )

    @Serializable
    data class ClerkSession(
        val id: String? = null,
        val status: String? = null,
        val user: ClerkUser? = null,
        @SerialName("last_active_token") val lastActiveToken: ClerkToken? = null,
    )

    @Serializable
    data class ClerkUser(
        val id: String? = null,
        @SerialName("first_name") val firstName: String? = null,
        @SerialName("last_name")  val lastName: String? = null,
        @SerialName("image_url")  val imageUrl: String? = null,
        @SerialName("has_image")  val hasImage: Boolean? = null,
        @SerialName("primary_email_address_id") val primaryEmailAddressId: String? = null,
        @SerialName("email_addresses") val emailAddresses: List<ClerkEmail>? = null,
    ) {
        /** The primary address when Clerk names one, else the first on file. */
        fun primaryEmail(): String? =
            emailAddresses?.firstOrNull { it.id != null && it.id == primaryEmailAddressId }?.emailAddress
                ?: emailAddresses?.firstOrNull()?.emailAddress
    }

    @Serializable
    data class ClerkEmail(
        val id: String? = null,
        @SerialName("email_address") val emailAddress: String? = null,
    )

    /** Shape of GET /v1/me and DELETE /v1/me. */
    @Serializable
    data class ClerkMeEnvelope(
        val response: ClerkUser? = null,
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
        val isNewUser: Boolean = false,
        val firstName: String? = null,
        val lastName: String? = null,
        val imageUrl: String? = null,
    ) : ClerkResult()

    data class Failure(val message: String) : ClerkResult()
}

/** Result of asking Clerk to begin a Google OAuth attempt. */
sealed class ClerkOAuthStart {
    /** [url] must be opened in a browser; Clerk redirects back to the app when done. */
    data class Success(val url: String) : ClerkOAuthStart()
    data class Failure(val message: String) : ClerkOAuthStart()
}

/** Result of deleting the signed-in user's own Clerk account. */
sealed class ClerkDeleteResult {
    object Success : ClerkDeleteResult()
    data class Failure(val message: String) : ClerkDeleteResult()
}
