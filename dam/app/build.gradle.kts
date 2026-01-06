plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.dam"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.dam"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // FIXED: Move composeOptions to the right place
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM - This will manage versions for most Compose dependencies
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended") // Use BOM version

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coil pour charger les images (optionnel)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation(libs.androidx.compose.foundation)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // REMOVE these duplicate dependencies - they're already included in the BOM
    // implementation("androidx.compose.ui:ui:1.7.4")
    // implementation("androidx.compose.ui:ui-tooling:1.7.4")
    // implementation("androidx.compose.foundation:foundation:1.7.4")
    // implementation("androidx.compose.material3:material3:1.2.1")
    // implementation("androidx.compose.animation:animation:1.7.4")
    // implementation("androidx.core:core-ktx:1.13.0") - duplicate
    // implementation("androidx.activity:activity-compose:1.9.0") - duplicate
}