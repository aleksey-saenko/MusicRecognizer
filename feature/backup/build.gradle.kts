plugins {
    alias(libs.plugins.musicrecognizer.android.feature)
    alias(libs.plugins.musicrecognizer.android.library.compose)
}

android {
    namespace = "com.mrsep.musicrecognizer.feature.backup"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)

    ksp(libs.moshi.codegen)
    implementation(libs.coil.compose)
}