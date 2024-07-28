import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    alias(libs.plugins.musicrecognizer.android.library)
    alias(libs.plugins.musicrecognizer.android.hilt)

    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.mrsep.musicrecognizer.data"

    defaultConfig {
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
            buildConfigField("boolean", "LOG_DEBUG_MODE", "false")
        }
    }

    ksp {
        arg("room.generateKotlin", "true")
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.strings)
    implementation(projects.core.common)

    implementation(libs.kotlinx.coroutinesAndroid)
    implementation(libs.kotlinx.collectionImmutable)

    implementation(libs.androidx.datastoreCore)
    implementation(libs.protobuf.kotlin.lite)

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

    implementation(libs.uuid.creator)

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

// workaround for https://github.com/google/ksp/issues/1590
// FIXME: remove when not needed anymore
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val capName = variant.name.capitalized()
            tasks.getByName<KotlinCompile>("ksp${capName}Kotlin") {
                setSource(tasks.getByName("generate${capName}Proto").outputs)
            }
        }
    }
}
