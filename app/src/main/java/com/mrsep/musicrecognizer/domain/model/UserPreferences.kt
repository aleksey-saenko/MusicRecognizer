package com.mrsep.musicrecognizer.domain.model

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val apiToken: String,
    val visibleLinks: VisibleLinks,
) {

    data class VisibleLinks(
        val spotify: Boolean,
        val appleMusic: Boolean,
        val deezer: Boolean,
        val napster: Boolean,
        val musicbrainz: Boolean
    )

}