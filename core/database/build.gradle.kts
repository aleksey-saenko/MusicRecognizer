plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.database"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    ksp {
        arg("room.generateKotlin", "true")
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.kotlinx.coroutinesAndroid)
    implementation(libs.androidx.core.ktx)
    api(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
}