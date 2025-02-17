plugins {
    alias(libs.plugins.musicrecognizer.android.feature)
    alias(libs.plugins.musicrecognizer.android.library.compose)
}

android {
    namespace = "com.mrsep.musicrecognizer.feature.library"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.coil.compose)
}