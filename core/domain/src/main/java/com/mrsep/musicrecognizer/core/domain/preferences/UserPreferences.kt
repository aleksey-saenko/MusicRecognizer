package com.mrsep.musicrecognizer.core.domain.preferences

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val currentRecognitionProvider: RecognitionProvider,
    val auddConfig: AuddConfig,
    val acrCloudConfig: AcrCloudConfig,
    val defaultAudioCaptureMode: AudioCaptureMode,
    val mainButtonLongPressAudioCaptureMode: AudioCaptureMode,
    val fallbackPolicy: FallbackPolicy,
    val recognizeOnStartup: Boolean,
    val requiredMusicServices: List<MusicService>,
    val notificationServiceEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val artworkBasedThemeEnabled: Boolean,
    val lyricsFontStyle: LyricsFontStyle,
    val trackFilter: TrackFilter,
    val hapticFeedback: HapticFeedback,
    val useGridForLibrary: Boolean,
    val useGridForRecognitionQueue: Boolean,
    val showRecognitionDateInLibrary: Boolean,
    val showCreationDateInQueue: Boolean,
    val themeMode: ThemeMode,
    val usePureBlackForDarkTheme: Boolean,
)

enum class AudioCaptureMode { Microphone, Device, Auto }

enum class FallbackAction(val save: Boolean, val launch: Boolean) {

    Ignore(false, false),
    Save(true, false),
    SaveAndLaunch(true, true);

    operator fun component1() = save
    operator fun component2() = launch
}

enum class ThemeMode { FollowSystem, AlwaysLight, AlwaysDark }

enum class FontSize { Small, Normal, Large, Huge }
