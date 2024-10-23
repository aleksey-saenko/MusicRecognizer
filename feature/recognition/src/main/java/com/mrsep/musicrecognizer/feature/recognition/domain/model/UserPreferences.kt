package com.mrsep.musicrecognizer.feature.recognition.domain.model

data class UserPreferences(
    val onboardingCompleted: Boolean,
    val currentRecognitionProvider: RecognitionProvider,
    val auddConfig: AuddConfig,
    val acrCloudConfig: AcrCloudConfig,
    val fallbackPolicy: FallbackPolicy,
    val defaultAudioCaptureMode: AudioCaptureMode,
    val mainButtonLongPressAudioCaptureMode: AudioCaptureMode,
    val hapticFeedback: HapticFeedback,
    val useGridForRecognitionQueue: Boolean,
    val showCreationDateInQueue: Boolean,
)

data class HapticFeedback(
    val vibrateOnTap: Boolean,
    val vibrateOnResult: Boolean,
)

data class FallbackPolicy(
    val noMatches: FallbackAction,
    val badConnection: FallbackAction,
    val anotherFailure: FallbackAction
)

enum class FallbackAction(val save: Boolean, val launch: Boolean) {

    Ignore(false, false),
    Save(true, false),
    SaveAndLaunch(true, true);

    operator fun component1() = save
    operator fun component2() = launch
}

enum class AudioCaptureMode {
    Microphone,
    Device,
    Auto,
}
