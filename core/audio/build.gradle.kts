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
}