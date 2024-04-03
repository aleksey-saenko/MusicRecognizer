plugins {
    alias(libs.plugins.musicrecognizer.android.feature)
    alias(libs.plugins.musicrecognizer.android.library.compose)
}

android {
    namespace = "com.mrsep.musicrecognizer.feature.track"
}

dependencies {
    implementation(libs.androidx.palette)
    implementation(libs.coil.compose)
    implementation(libs.materialKolor)
}