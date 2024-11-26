@file:Suppress(names = ["UnstableApiUsage", "SpellCheckingInspection"])

import java.util.Properties

plugins {
    alias(libs.plugins.musicrecognizer.android.application)
    alias(libs.plugins.musicrecognizer.android.application.compose)
    alias(libs.plugins.musicrecognizer.android.hilt)
    alias(libs.plugins.aboutLibraries)
}

android {
    namespace = "com.mrsep.musicrecognizer"

    defaultConfig {
        applicationId = "com.mrsep.musicrecognizer"
        versionCode = 26
        versionName = "1.7.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties().apply {
            load(rootProject.file("local.properties").reader())
        }
        val devOptionsEnabled = properties["dev.options"]?.toString() ?: "false"
        buildConfigField("boolean", "DEV_OPTIONS", devOptionsEnabled)

        // Excluded unfinished translations: "fa", "gu", "pl"
        resourceConfigurations += listOf("en", "cs", "de", "es", "fr", "it", "pt-rBR", "ru", "sk", "tr")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Audile[Debug]")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            resources.excludes += "DebugProbesKt.bin"
        }
    }
    androidResources {
        generateLocaleConfig = true
    }
    aboutLibraries {
        registerAndroidTasks = false
        excludeFields = arrayOf("funding", "scm")
        additionalLicenses = arrayOf("GPL-3.0-or-later")
        gitHubApiToken = properties["github.token"] as? String
        prettyPrint = true
    }
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    implementation(projects.data)
    implementation(projects.core.ui)
    implementation(projects.core.strings)
    implementation(projects.core.common)
    implementation(projects.feature.library)
    implementation(projects.feature.track)
    implementation(projects.feature.recognition)
    implementation(projects.feature.preferences)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.developerMode)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtimeKtx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.windowManager)
    implementation(libs.androidx.compose.material3.windowSizeClass)

    implementation(libs.kotlinx.coroutinesAndroid)
    implementation(libs.kotlinx.collectionImmutable)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(libs.androidx.workKtx)
    implementation(libs.hilt.ext.work)
    ksp(libs.hilt.ext.compiler)

    implementation(libs.acra.core)
    implementation(libs.acra.mail)
    implementation(libs.acra.dialog)
    compileOnly(libs.auto.service.annotations)
    ksp(libs.auto.service.ksp)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
