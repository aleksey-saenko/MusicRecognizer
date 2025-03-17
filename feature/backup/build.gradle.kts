plugins {
    alias(libs.plugins.musicrecognizer.android.feature)
    alias(libs.plugins.musicrecognizer.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mrsep.musicrecognizer.feature.backup"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)

    implementation(libs.kotlinx.serializationJson)
    implementation(libs.coil.compose)
}