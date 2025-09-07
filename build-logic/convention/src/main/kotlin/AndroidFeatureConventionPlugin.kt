import com.android.build.gradle.LibraryExtension
import com.mrsep.musicrecognizer.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("musicrecognizer.android.library")
                apply("musicrecognizer.hilt")
            }
            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
            }

            dependencies {
                listOf(
                    ":core:ui",
                    ":core:strings",
                    ":core:common",
                ).forEach {
                    add("implementation", project(it))
                }

                listOf(
                    "androidx.navigation.compose",
                    "androidx.hilt.navigation.compose",

                    "androidx.core.ktx",
                    "androidx.activity.compose",
                    "androidx.lifecycle.runtimeKtx",
                    "androidx.lifecycle.runtime.compose",
                    "androidx.lifecycle.viewmodel.compose",

                    "kotlinx.coroutinesAndroid",
                    "kotlinx.collectionImmutable",
                ).forEach {
                    add("implementation", libs.findLibrary(it).get())
                }

                listOf(
                    "androidx.test.ext.junit",
                    "androidx.test.espresso.core",
                ).forEach {
                    add("androidTestImplementation", libs.findLibrary(it).get())
                }

                listOf(
                    "junit4",
                ).forEach {
                    add("testImplementation", libs.findLibrary(it).get())
                }
            }
        }
    }
}
