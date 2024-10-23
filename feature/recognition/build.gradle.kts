plugins {
    alias(libs.plugins.musicrecognizer.android.feature)
    alias(libs.plugins.musicrecognizer.android.library.compose)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.mrsep.musicrecognizer.feature.recognition"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.accompanist.permissions)
    implementation(libs.coil.compose)

    implementation(libs.androidx.workKtx)
    implementation(libs.hilt.ext.work)
    ksp(libs.hilt.ext.compiler)

    implementation(libs.androidx.glance)
    implementation(libs.androidx.glanceAppWidget)
    implementation(libs.androidx.glanceMaterial3)

    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
}