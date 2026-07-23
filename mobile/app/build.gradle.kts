plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.powertrack.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.powertrack.mobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Backend local de dev corre en :8081 (backend/src/main/resources/application-dev.yml).
            // 10.0.2.2 es el alias que el emulador de Android usa para el localhost del host.
            // Para dispositivo físico por USB: "adb reverse tcp:8081 tcp:8081" y cambiar
            // este valor a "http://127.0.0.1:8081/" (network_security_config ya permite ambos).
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8081/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // En release NO hay cleartext permitido: esta URL placeholder se reemplaza por la
            // URL real de producción (HTTPS) cuando exista un backend desplegado.
            buildConfigField("String", "BASE_URL", "\"https://api.powertrack.example.com/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Compose ---
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    // collectAsStateWithLifecycle() para observar StateFlow de los ViewModel.
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Set extendido de íconos: el core (material-icons-core, transitivo de
    // material3) no trae FitnessCenter/TrendingUp/Logout/ChevronRight/etc.
    // que necesitan las 5 pantallas reales (bottom nav, Live Tracker, Perfil).
    implementation("androidx.compose.material:material-icons-extended")

    // Fuentes descargables de Google Fonts (Anton + Inter) — no se bundlean .ttf.
    implementation("androidx.compose.ui:ui-text-google-fonts")

    // --- Navegación ---
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // --- Hilt (DI) ---
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // --- Red: Retrofit + OkHttp + kotlinx.serialization ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- Almacenamiento seguro de tokens (PRD §13.1) ---
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // --- Debug tooling ---
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
