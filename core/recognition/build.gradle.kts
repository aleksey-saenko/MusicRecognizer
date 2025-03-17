plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.recognition"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.network)

    implementation(libs.kotlinx.serializationJson)
    implementation(libs.uuid.creator)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockWebServer)
}