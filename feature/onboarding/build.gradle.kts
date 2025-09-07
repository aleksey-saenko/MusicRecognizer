plugins {
    alias(libs.plugins.musicrecognizer.android.feature)
    alias(libs.plugins.musicrecognizer.android.library.compose)
}

android {
    namespace = "com.mrsep.musicrecognizer.feature.onboarding"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.accompanist.permissions)
}