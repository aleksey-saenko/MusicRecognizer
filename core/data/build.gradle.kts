plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.data"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.recognition)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(projects.core.common)

    implementation(libs.kotlinx.coroutinesAndroid)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.media3.exoplayer)
}