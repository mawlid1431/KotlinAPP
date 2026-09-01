package com.aiu.tdminsight.data.supabase

import com.aiu.tdminsight.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.*

/**
 * SupabaseClientProvider — creates the single Supabase client instance.
 *
 * Setup required in the Supabase Dashboard (https://supabase.com):
 *   1. Add the SUPABASE_URL and SUPABASE_ANON_KEY to local.properties.
 *   2. Run the SQL in supabase/schema.sql to create tables + RLS policies.
 */
object SupabaseClientProvider {

    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    fun create() = createSupabaseClient(
        supabaseUrl  = BuildConfig.SUPABASE_URL.ifBlank { "https://placeholder.supabase.co" },
        supabaseKey  = BuildConfig.SUPABASE_ANON_KEY.ifBlank { "placeholder" },
    ) {
        install(Postgrest)
        httpEngine = OkHttp.create()
    }
}
