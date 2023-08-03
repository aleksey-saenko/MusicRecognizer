@file:Suppress(names = ["UnstableApiUsage", "SpellCheckingInspection"])
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.mrsep.musicrecognizer.feature.recognition"
    compileSdk = libs.versions.sdkCompile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdkMin.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"

        freeCompilerArgs = freeCompilerArgs + listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                    project.buildDir.absolutePath + "/compose_metrics"
        )
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    project.buildDir.absolutePath + "/compose_metrics"
        )
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:strings"))
    implementation(project(":core:common"))

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.toolingPreview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.testManifest)
    androidTestImplementation(libs.androidx.compose.ui.testJunit)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtimeKtx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.kotlinx.coroutinesAndroid)
    implementation(libs.kotlinx.collectionImmutable)

    implementation(libs.accompanist.permissions)

    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.workKtx)
    implementation(libs.androidx.lifecycle.livedataKtx)
    implementation(libs.hilt.ext.work)
    kapt(libs.hilt.ext.compiler)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}