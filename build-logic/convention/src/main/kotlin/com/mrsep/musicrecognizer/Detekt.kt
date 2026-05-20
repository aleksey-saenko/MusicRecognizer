package com.mrsep.musicrecognizer

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

internal fun Project.configureDetekt(extension: DetektExtension) = extension.apply {
    tasks.withType<Detekt>().configureEach {
        reports {
            checkstyle.required.set(false)
            html.required.set(true)
            sarif.required.set(false)
            markdown.required.set(false)
        }
    }
    dependencies {
        "detektPlugins"(libs.findLibrary("detekt-compose").get())
    }
}
