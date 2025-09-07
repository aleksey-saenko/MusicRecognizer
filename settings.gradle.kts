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
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:data")
include(":core:audio")
include(":core:recognition")
include(":core:domain")
include(":core:strings")
include(":core:ui")
include(":core:common")
include(":feature:recognition")
include(":feature:track")
include(":feature:library")
include(":feature:preferences")
include(":feature:backup")
include(":feature:onboarding")
include(":feature:developer-mode")