import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

// Read credentials from local.properties.
val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        f.inputStream().use { props.load(it) }
    }
}

val clerkKey = localProps.getProperty("CLERK_PUBLISHABLE_KEY").takeUnless { it.isNullOrBlank() }
    ?: "pk_test_cmVhbC1hc3AtNjI1NS5jbGVyay5hY2NvdW50cy5kZXYk"
val supabaseUrl = localProps.getProperty("SUPABASE_URL").takeUnless { it.isNullOrBlank() }
    ?: "https://mhijnqqdichqsfplwfaa.supabase.co"
val supabaseAnonKey = localProps.getProperty("SUPABASE_ANON_KEY").takeUnless { it.isNullOrBlank() }
    ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1oaWpucXFkaWNocXNmcGx3ZmFhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODgyNzE3MzIsImV4cCI6MjEwMzg0NzczMn0.1HhpMhdKfm_qEFi-dC2c_3b8wEsFmzjsDcyJP8fUEh4"

android {
    namespace = "com.aiu.tdminsight"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aiu.tdminsight"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject credentials into BuildConfig.
        buildConfigField("String", "CLERK_PUBLISHABLE_KEY", "\"$clerkKey\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // ── Existing Compose / AndroidX ─────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // ── Supabase Kotlin SDK ─────────────────────────────────────────────────
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)

    // ── Ktor HTTP client (required by Supabase + used for Clerk REST calls) ─
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // ── Image loading (remote Clerk avatars) ───────────────────────────────
    implementation(libs.coil.compose)

    // ── Kotlinx ────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // ── Tests ──────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
