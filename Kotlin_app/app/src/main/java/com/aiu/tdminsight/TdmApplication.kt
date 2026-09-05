package com.aiu.tdminsight

import android.app.Application
import com.aiu.tdminsight.auth.AuthRepository
import com.aiu.tdminsight.auth.ClerkAuthManager
import com.aiu.tdminsight.data.supabase.SupabaseClientProvider
import com.aiu.tdminsight.data.supabase.SupabaseRepository

/**
 * TdmApplication — singleton host for infrastructure objects.
 *
 * ViewModels retrieve dependencies here via (application as TdmApplication).
 * This avoids a full DI framework while keeping singletons properly scoped
 * to the Application lifecycle.
 *
 * Credentials are read at runtime from BuildConfig, which is populated from
 * local.properties at build time. Leave the keys blank until you are ready
 * to integrate the real services.
 */
class TdmApplication : Application() {

    // ── Lazy singletons — initialized only when first accessed ────────────

    val supabaseClient by lazy { SupabaseClientProvider.create() }

    /** Shared by Clerk (device token) and AuthRepository (session) — one store. */
    private val authPrefs by lazy { getSharedPreferences("tdm_auth", MODE_PRIVATE) }

    val clerkAuthManager by lazy {
        ClerkAuthManager(BuildConfig.CLERK_PUBLISHABLE_KEY, authPrefs)
    }

    val authRepository by lazy {
        AuthRepository(
            clerk = clerkAuthManager,
            prefs = authPrefs,
        )
    }

    val supabaseRepository by lazy {
        SupabaseRepository(supabaseClient, authRepository)
    }
}
