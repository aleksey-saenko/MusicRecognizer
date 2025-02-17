plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.common"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.strings)

    implementation(libs.kotlinx.coroutinesAndroid)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.navigation.compose)
}