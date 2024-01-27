import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.ksp)
    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.mrsep.musicrecognizer.data"
    compileSdk = libs.versions.sdkCompile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdkMin.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val properties = Properties().apply {
            load(rootProject.file("local.properties").reader())
        }
        val auddApiToken = properties["audd.api.token"]?.toString() ?: "\"\""
        val acrCloudHost = properties["acr.cloud.host"]?.toString() ?: "\"\""
        val acrCloudAccessKey = properties["acr.cloud.access.key"]?.toString() ?: "\"\""
        val acrCloudAccessSecret = properties["acr.cloud.access.secret"]?.toString() ?: "\"\""

        buildConfigField("String", "AUDD_TOKEN", auddApiToken)
        buildConfigField("String", "ACR_CLOUD_HOST", acrCloudHost)
        buildConfigField("String", "ACR_CLOUD_ACCESS_KEY", acrCloudAccessKey)
        buildConfigField("String", "ACR_CLOUD_ACCESS_SECRET", acrCloudAccessSecret)

        buildConfigField("boolean", "LOG_DEBUG_MODE", "false")
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "LOG_DEBUG_MODE", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "LOG_DEBUG_MODE", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    ksp {
        arg("room.generateKotlin", "true")
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            resources.excludes += "DebugProbesKt.bin"
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":core:strings"))
    implementation(project(":core:common"))

    implementation(libs.kotlinx.coroutinesAndroid)
    implementation(libs.kotlinx.collectionImmutable)

    implementation(libs.androidx.datastoreCore)
    implementation(libs.protobuf.kotlin.lite)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.loggingInterceptor)
    implementation(libs.okhttp.coroutines)

    implementation(libs.moshi.core)
    implementation(libs.moshi.adapters)
    ksp(libs.moshi.codegen)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockWebServer)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}