package com.mrsep.musicrecognizer.data.preferences

import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo

data class UserPreferencesDo(
    val onboardingCompleted: Boolean,
    val currentRecognitionProvider: RecognitionProviderDo,
    val auddConfig: AuddConfigDo,
    val acrCloudConfig: AcrCloudConfigDo,
    val requiredMusicServices: List<MusicServiceDo>,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val artworkBasedThemeEnabled: Boolean,
    val developerModeEnabled: Boolean,
    val fallbackPolicy: FallbackPolicyDo,
    val lyricsFontStyle: LyricsFontStyleDo,
    val trackFilter: TrackFilterDo,
    val hapticFeedback: HapticFeedbackDo,
    val useGridForLibrary: Boolean,
    val useGridForRecognitionQueue: Boolean,
    val showRecognitionDateInLibrary: Boolean,
    val showCreationDateInQueue: Boolean,
    val themeMode: ThemeModeDo,
    val usePureBlackForDarkTheme: Boolean,
    val recognizeOnStartup: Boolean,
) {

    data class FallbackPolicyDo(
        val noMatches: FallbackActionDo,
        val badConnection: FallbackActionDo,
        val anotherFailure: FallbackActionDo
    )

    data class LyricsFontStyleDo(
        val fontSize: FontSizeDo,
        val isBold: Boolean,
        val isHighContrast: Boolean,
        val alignToStart: Boolean,
    )

    data class TrackFilterDo(
        val favoritesMode: FavoritesModeDo,
        val sortBy: SortByDo,
        val orderBy: OrderByDo,
        val dateRange: LongRange,
    )

    data class HapticFeedbackDo(
        val vibrateOnTap: Boolean,
        val vibrateOnResult: Boolean,
    )
}

enum class FallbackActionDo { Ignore, Save, SaveAndLaunch }
enum class FontSizeDo { Small, Normal, Large, Huge }

enum class FavoritesModeDo { All, OnlyFavorites, ExcludeFavorites }
enum class SortByDo { RecognitionDate, Title, Artist, ReleaseDate }
enum class OrderByDo { Asc, Desc }

enum class ThemeModeDo { FollowSystem, AlwaysLight, AlwaysDark }
