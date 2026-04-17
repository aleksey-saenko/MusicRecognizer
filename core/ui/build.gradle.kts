plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.android.library.compose)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.ui"
}

dependencies {
    implementation(projects.core.strings)
    implementation(projects.core.domain)

    implementation(libs.androidx.palette)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
}