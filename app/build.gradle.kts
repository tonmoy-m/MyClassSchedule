plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // keep KSP
}

android {
    namespace = "bd.edu.my_class_schedule"
    compileSdk = 36

    defaultConfig {
        applicationId = "bd.edu.my_class_schedule"
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // AndroidX + Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // For LazyColumn + KeyboardOptions/Type/ImeAction
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation + ViewModel
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room (KSP)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Ensure KSP worker sees Kotlin stdlib 2.0.21 (fixes SpillingKt CNFE)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    ksp("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    ksp("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Desugaring (needed for java.time in converters)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Tests/tooling
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Remove any ksp { arg(...) } block and any resolutionStrategy blocks.