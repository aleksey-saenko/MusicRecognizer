package com.mrsep.musicrecognizer.core.data.preferences

import com.mrsep.musicrecognizer.core.datastore.AudioCaptureModeProto
import com.mrsep.musicrecognizer.core.datastore.MusicServiceProto
import com.mrsep.musicrecognizer.core.datastore.RecognitionProviderProto
import com.mrsep.musicrecognizer.core.datastore.UserPreferencesProto
import com.mrsep.musicrecognizer.core.domain.preferences.AcrCloudConfig
import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.domain.preferences.FallbackAction
import com.mrsep.musicrecognizer.core.domain.preferences.FallbackPolicy
import com.mrsep.musicrecognizer.core.domain.preferences.FavoritesMode
import com.mrsep.musicrecognizer.core.domain.preferences.FontSize
import com.mrsep.musicrecognizer.core.domain.preferences.HapticFeedback
import com.mrsep.musicrecognizer.core.domain.preferences.LyricsFontStyle
import com.mrsep.musicrecognizer.core.domain.preferences.OrderBy
import com.mrsep.musicrecognizer.core.domain.preferences.SortBy
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.preferences.TrackFilter
import com.mrsep.musicrecognizer.core.domain.preferences.UserPreferences
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService

internal fun UserPreferencesProto.toDomain() = UserPreferences(
    onboardingCompleted = onboardingCompleted,
    currentRecognitionProvider = when (currentRecognitionProvider!!) {
        RecognitionProviderProto.Audd -> RecognitionProvider.Audd
        RecognitionProviderProto.AcrCloud -> RecognitionProvider.AcrCloud
        RecognitionProviderProto.UNRECOGNIZED -> error("Unexpected proto value")
    },
    auddConfig = AuddConfig(apiToken = apiToken),
    acrCloudConfig = AcrCloudConfig(
        host = acrCloudConfig.host,
        accessKey = acrCloudConfig.accessKey,
        accessSecret = acrCloudConfig.accessSecret
    ),
    defaultAudioCaptureMode = defaultAudioCaptureMode.toDomain(),
    mainButtonLongPressAudioCaptureMode = mainButtonLongPressAudioCaptureMode.toDomain(),
    fallbackPolicy = FallbackPolicy(
        noMatches = fallbackPolicy.noMatches.toDomain(),
        badConnection = fallbackPolicy.badConnection.toDomain(),
        anotherFailure = fallbackPolicy.anotherFailure.toDomain()
    ),
    recognizeOnStartup = recognizeOnStartup,
    requiredMusicServices = requiredMusicServicesList.map { service ->
        when (service!!) {
            MusicServiceProto.AmazonMusic -> MusicService.AmazonMusic
            MusicServiceProto.Anghami -> MusicService.Anghami
            MusicServiceProto.AppleMusic -> MusicService.AppleMusic
            MusicServiceProto.Audiomack -> MusicService.Audiomack
            MusicServiceProto.Audius -> MusicService.Audius
            MusicServiceProto.Boomplay -> MusicService.Boomplay
            MusicServiceProto.Deezer -> MusicService.Deezer
            MusicServiceProto.MusicBrainz -> MusicService.MusicBrainz
            MusicServiceProto.Napster -> MusicService.Napster
            MusicServiceProto.Pandora -> MusicService.Pandora
            MusicServiceProto.Soundcloud -> MusicService.Soundcloud
            MusicServiceProto.Spotify -> MusicService.Spotify
            MusicServiceProto.Tidal -> MusicService.Tidal
            MusicServiceProto.YandexMusic -> MusicService.YandexMusic
            MusicServiceProto.Youtube -> MusicService.Youtube
            MusicServiceProto.YoutubeMusic -> MusicService.YoutubeMusic
            MusicServiceProto.UNRECOGNIZED -> error("Unexpected proto value")
        }
    },
    notificationServiceEnabled = notificationServiceEnabled,
    dynamicColorsEnabled = dynamicColorsEnabled,
    artworkBasedThemeEnabled = artworkBasedThemeEnabled,
    lyricsFontStyle = LyricsFontStyle(
        fontSize = when (lyricsFontStyle.fontSize!!) {
            UserPreferencesProto.FontSizeProto.SMALL -> FontSize.Small
            UserPreferencesProto.FontSizeProto.NORMAL -> FontSize.Normal
            UserPreferencesProto.FontSizeProto.LARGE -> FontSize.Large
            UserPreferencesProto.FontSizeProto.HUGE -> FontSize.Huge
            UserPreferencesProto.FontSizeProto.UNRECOGNIZED -> error("Unexpected proto value")
        },
        isBold = lyricsFontStyle.isBold,
        isHighContrast = lyricsFontStyle.isHighContrast,
        alignToStart = lyricsFontStyle.alignToStart
    ),
    trackFilter = TrackFilter(
        favoritesMode = when (trackFilter.favoritesMode!!) {
            UserPreferencesProto.FavoritesModeProto.ALL -> FavoritesMode.All
            UserPreferencesProto.FavoritesModeProto.ONLY_FAVORITES -> FavoritesMode.OnlyFavorites
            UserPreferencesProto.FavoritesModeProto.EXCLUDE_FAVORITES -> FavoritesMode.ExcludeFavorites
            UserPreferencesProto.FavoritesModeProto.UNRECOGNIZED -> error("Unexpected proto value")
        },
        sortBy = when (trackFilter.sortBy!!) {
            UserPreferencesProto.SortByProto.RECOGNITION_DATE -> SortBy.RecognitionDate
            UserPreferencesProto.SortByProto.TITLE -> SortBy.Title
            UserPreferencesProto.SortByProto.ARTIST -> SortBy.Artist
            UserPreferencesProto.SortByProto.RELEASE_DATE -> SortBy.ReleaseDate
            UserPreferencesProto.SortByProto.UNRECOGNIZED -> error("Unexpected proto value")
        },
        orderBy = when (trackFilter.orderBy!!) {
            UserPreferencesProto.OrderByProto.ASC -> OrderBy.Asc
            UserPreferencesProto.OrderByProto.DESC -> OrderBy.Desc
            UserPreferencesProto.OrderByProto.UNRECOGNIZED -> error("Unexpected proto value")
        },
        dateRange = trackFilter.startDate..trackFilter.endDate
    ),
    hapticFeedback = HapticFeedback(
        vibrateOnTap = hapticFeedback.vibrateOnTap,
        vibrateOnResult = hapticFeedback.vibrateOnResult
    ),
    useGridForLibrary = useGridForLibrary,
    useGridForRecognitionQueue = useGridForLibrary,
    showRecognitionDateInLibrary = showRecognitionDateInLibrary,
    showCreationDateInQueue = showCreationDateInQueue,
    themeMode = when (themeMode!!) {
        UserPreferencesProto.ThemeModeProto.FOLLOW_SYSTEM -> ThemeMode.FollowSystem
        UserPreferencesProto.ThemeModeProto.ALWAYS_LIGHT -> ThemeMode.AlwaysLight
        UserPreferencesProto.ThemeModeProto.ALWAYS_DARK -> ThemeMode.AlwaysDark
        UserPreferencesProto.ThemeModeProto.UNRECOGNIZED -> error("Unexpected proto value")
    },
    usePureBlackForDarkTheme = usePureBlackForDarkTheme
)

internal fun AudioCaptureModeProto.toDomain() = when (this) {
    AudioCaptureModeProto.Unspecified -> AudioCaptureMode.Microphone
    AudioCaptureModeProto.Microphone -> AudioCaptureMode.Microphone
    AudioCaptureModeProto.Device -> AudioCaptureMode.Device
    AudioCaptureModeProto.Auto -> AudioCaptureMode.Auto
    AudioCaptureModeProto.UNRECOGNIZED -> error("Unexpected proto value")
}

internal fun UserPreferencesProto.FallbackActionProto.toDomain() = when (this) {
    UserPreferencesProto.FallbackActionProto.IGNORE -> FallbackAction.Ignore
    UserPreferencesProto.FallbackActionProto.SAVE -> FallbackAction.Save
    UserPreferencesProto.FallbackActionProto.SAVE_AND_LAUNCH -> FallbackAction.SaveAndLaunch
    UserPreferencesProto.FallbackActionProto.UNRECOGNIZED -> error("Unexpected proto value")
}
