plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.recognition"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.network)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.kotlinx.serializationJson)
    implementation(libs.uuid.creator)

    testImplementation(libs.junit4)
    testImplementation(libs.kotest)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockWebServer)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.slf4j.simple)

    androidTestImplementation(libs.kotest)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}