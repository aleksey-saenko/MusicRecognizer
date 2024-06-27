package com.mrsep.musicrecognizer.feature.track.domain.model

data class UserPreferences(
    val requiredMusicServices: List<MusicService>,
    val lyricsFontStyle: LyricsFontStyle,
    val artworkBasedThemeEnabled: Boolean,
    val themeMode: ThemeMode,
    val usePureBlackForDarkTheme: Boolean,
) {

    data class LyricsFontStyle(
        val fontSize: FontSize,
        val isBold: Boolean,
        val isHighContrast: Boolean,
        val alignToStart: Boolean
    )
}

enum class FontSize {
    Small, Normal, Large, Huge
}

enum class ThemeMode { FollowSystem, AlwaysLight, AlwaysDark }
