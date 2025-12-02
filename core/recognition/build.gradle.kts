@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.recognition"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }
        ndkVersion = "29.0.14206865"
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/vibrafp/lib/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DENABLE_LTO=ON",
                        "-DCMAKE_BUILD_TYPE=Release"
                    )
                }
            }
            ndk {
                debugSymbolLevel = "NONE"
            }
        }
        debug {
            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DENABLE_LTO=OFF",
                        "-DCMAKE_BUILD_TYPE=Debug"
                    )
                }
            }
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.audio)

    implementation(libs.kotlinx.serializationJson)
    implementation(libs.uuidCreator)

    testImplementation(libs.junit4)
    testImplementation(libs.kotest)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockWebServer)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.slf4j.simple)

    androidTestImplementation(libs.kotest)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}