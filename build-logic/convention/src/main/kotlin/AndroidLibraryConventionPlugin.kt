import com.android.build.gradle.LibraryExtension
import com.mrsep.musicrecognizer.configureDetekt
import com.mrsep.musicrecognizer.configureKotlinAndroid
import com.mrsep.musicrecognizer.libs
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("io.gitlab.arturbosch.detekt")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            configureDetekt(extensions.getByType<DetektExtension>())

            dependencies {
                add("testImplementation", libs.findLibrary("junit4").get())
                add("androidTestImplementation", libs.findLibrary("androidx.test.ext.junit").get())
            }
        }
    }
}
