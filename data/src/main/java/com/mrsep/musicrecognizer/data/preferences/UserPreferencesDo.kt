package com.mrsep.musicrecognizer.data.preferences

data class UserPreferencesDo(
    val onboardingCompleted: Boolean,
    val apiToken: String,
    val requiredServices: RequiredServicesDo,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val artworkBasedThemeEnabled: Boolean,
    val developerModeEnabled: Boolean,
    val fallbackPolicy: FallbackPolicyDo,
    val lyricsFontStyle: LyricsFontStyleDo,
    val trackFilter: TrackFilterDo
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

    data class FallbackPolicyDo(
        val noMatches: FallbackActionDo,
        val badConnection: FallbackActionDo,
        val anotherFailure: FallbackActionDo
    )

    data class LyricsFontStyleDo(
        val fontSize: FontSizeDo,
        val isBold: Boolean,
        val isHighContrast: Boolean
    )

    data class TrackFilterDo(
        val favoritesMode: FavoritesModeDo,
        val sortBy: SortByDo,
        val orderBy: OrderByDo,
        val dateRange: LongRange,
    )

}

enum class FallbackActionDo { Ignore, Save, SaveAndLaunch }
enum class FontSizeDo { Small, Normal, Large, Huge }

enum class FavoritesModeDo { All, OnlyFavorites, ExcludeFavorites }
enum class SortByDo { RecognitionDate, Title, Artist, ReleaseDate }
enum class OrderByDo { Asc, Desc }