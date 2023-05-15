package com.mrsep.musicrecognizer.feature.recognition.domain.model

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val apiToken: String,
    val requiredServices: RequiredServices,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val developerModeEnabled: Boolean,
    val schedulePolicy: SchedulePolicy
) {

    data class RequiredServices(
        val spotify: Boolean,
        val youtube: Boolean,
        val soundCloud: Boolean,
        val appleMusic: Boolean,
        val deezer: Boolean,
        val napster: Boolean,
        val musicbrainz: Boolean
    )

    data class SchedulePolicy(
        val noMatches: ScheduleAction,
        val badConnection: ScheduleAction,
        val anotherFailure: ScheduleAction
    )

}


enum class ScheduleAction(val save: Boolean, val launch: Boolean) {
    Ignore(false, false), Save(true, false), SaveAndLaunch(true, true);

    operator fun component1() = save
    operator fun component2() = launch
}