package com.mrsep.musicrecognizer.feature.track.domain.model

data class UserPreferences(
    val requiredServices: RequiredServices,
    val lyricsFontStyle: LyricsFontStyle,
    val artworkBasedThemeEnabled: Boolean
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

    data class LyricsFontStyle(
        val fontSize: FontSize,
        val isBold: Boolean,
        val isHighContrast: Boolean
    )

}

enum class FontSize {
    Small, Normal, Large, Huge
}