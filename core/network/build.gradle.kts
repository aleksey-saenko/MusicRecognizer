plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.network"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.common)

    api(libs.kotlinx.serializationJson)
    api(libs.coil.network.ktor)

    implementation(platform(libs.okhttp.bom))
    api(libs.ktor.client.core)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.okhttp)
    api(libs.ktor.client.websockets)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
}
