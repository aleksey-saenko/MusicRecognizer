@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "MusicRecognizer"
include(":app")
include(":feature:library")
include(":data")
include(":feature:preferences")
include(":core:strings")
include(":core:ui")
include(":feature:recognition")
include(":core:common")
include(":feature:onboarding")
include(":feature:track")
include(":feature:developer-mode")