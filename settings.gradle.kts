@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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
include(":core:audio")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:domain")
include(":core:metadata")
include(":core:network")
include(":core:recognition")
include(":core:strings")
include(":core:ui")
include(":feature:recognition")
include(":feature:track")
include(":feature:library")
include(":feature:preferences")
include(":feature:backup")
include(":feature:onboarding")
include(":feature:developer-mode")