package com.mrsep.musicrecognizer.data.preferences

data class UserPreferencesDo(
    val onboardingCompleted: Boolean,
    val apiToken: String,
    val requiredServices: RequiredServicesDo,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val developerModeEnabled: Boolean,
    val schedulePolicy: SchedulePolicyDo,
    val lyricsFontStyle: LyricsFontStyleDo
) {

    data class RequiredServicesDo(
        val spotify: Boolean,
        val youtube: Boolean,
        val soundCloud: Boolean,
        val appleMusic: Boolean,
        val deezer: Boolean,
        val napster: Boolean,
        val musicbrainz: Boolean
    )

    data class SchedulePolicyDo(
        val noMatches: ScheduleActionDo,
        val badConnection: ScheduleActionDo,
        val anotherFailure: ScheduleActionDo
    )

    data class LyricsFontStyleDo(
        val fontSize: FontSizeDo,
        val isBold: Boolean,
        val isHighContrast: Boolean
    )

}

enum class ScheduleActionDo {
    Ignore,
    Save,
    SaveAndLaunch
}

enum class FontSizeDo {
    Small, Normal, Large, Huge
}