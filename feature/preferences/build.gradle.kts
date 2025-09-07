plugins {
    alias(libs.plugins.musicrecognizer.android.feature)
    alias(libs.plugins.musicrecognizer.android.library.compose)
}

android {
    namespace = "com.mrsep.musicrecognizer.feature.preferences"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.accompanist.permissions)
    implementation(libs.coil.compose)
    implementation(libs.aboutLibraries.core)
    implementation(libs.aboutLibraries.material3)
}