plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.audio"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(libs.kotlinx.coroutinesAndroid)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.extractor)
    implementation(libs.androidx.media3.container)
    implementation(libs.androidx.media3.muxer)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutinesTest)
}