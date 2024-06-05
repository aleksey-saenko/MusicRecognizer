import com.android.build.api.dsl.ApplicationExtension
import com.mrsep.musicrecognizer.configureDetekt
import com.mrsep.musicrecognizer.configureKotlinAndroid
import com.mrsep.musicrecognizer.libs
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("io.gitlab.arturbosch.detekt")
            }

            configureDetekt(extensions.getByType<DetektExtension>())

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.findVersion("sdkTarget").get().requiredVersion.toInt()
            }
        }
    }
}
