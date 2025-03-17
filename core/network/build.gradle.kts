plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.network"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("boolean", "LOG_DEBUG_MODE", "false")
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "LOG_DEBUG_MODE", "true")
        }
        release {
            buildConfigField("boolean", "LOG_DEBUG_MODE", "false")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.common)

    api(libs.kotlinx.serializationJson)
    implementation(platform(libs.okhttp.bom))
    api(libs.okhttp.core)
    api(libs.okhttp.loggingInterceptor)
    api(libs.okhttp.coroutines)
}