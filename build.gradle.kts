import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// FIXME: delete after upgrade to Gradle 8.1 https://github.com/gradle/gradle/issues/22797
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.protobuf) apply false
}

//gradle 8.0 uses jdk 17 while kapt requires same as jvmTarget (1.8 in this project)
//https://youtrack.jetbrains.com/issue/KT-55947
allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}