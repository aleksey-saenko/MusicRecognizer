import java.util.Properties

plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.mrsep.musicrecognizer.core.datastore"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val auddApiToken = localProperties["audd.api.token"]?.toString() ?: "\"\""
        val acrCloudHost = localProperties["acr.cloud.host"]?.toString() ?: "\"\""
        val acrCloudAccessKey = localProperties["acr.cloud.access.key"]?.toString() ?: "\"\""
        val acrCloudAccessSecret = localProperties["acr.cloud.access.secret"]?.toString() ?: "\"\""

        buildConfigField("String", "AUDD_TOKEN", auddApiToken)
        buildConfigField("String", "ACR_CLOUD_HOST", acrCloudHost)
        buildConfigField("String", "ACR_CLOUD_ACCESS_KEY", acrCloudAccessKey)
        buildConfigField("String", "ACR_CLOUD_ACCESS_SECRET", acrCloudAccessSecret)
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutinesAndroid)

    api(libs.androidx.datastoreCore)
    api(libs.protobuf.kotlin.lite)
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
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
