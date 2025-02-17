plugins {
    alias(libs.plugins.musicrecognizer.jvm.library)
    alias(libs.plugins.musicrecognizer.hilt)
}

dependencies {
    implementation(libs.kotlinx.coroutinesCore)
}