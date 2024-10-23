package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.AudioCaptureModeDo
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AcrCloudConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AuddConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioCaptureMode
import com.mrsep.musicrecognizer.feature.recognition.domain.model.FallbackAction
import com.mrsep.musicrecognizer.feature.recognition.domain.model.FallbackPolicy
import com.mrsep.musicrecognizer.feature.recognition.domain.model.HapticFeedback
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionProvider
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import javax.inject.Inject

class UserPreferencesMapper @Inject constructor(
    private val fallbackActionMapper: Mapper<FallbackActionDo, FallbackAction>,
    private val providerMapper: BidirectionalMapper<RecognitionProviderDo, RecognitionProvider>,
    private val auddConfigMapper: BidirectionalMapper<AuddConfigDo, AuddConfig>,
    private val acrCloudConfigMapper: BidirectionalMapper<AcrCloudConfigDo, AcrCloudConfig>,
    private val audioCaptureModeMapper: BidirectionalMapper<AudioCaptureModeDo, AudioCaptureMode>,
) :
    Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            currentRecognitionProvider = providerMapper.map(input.currentRecognitionProvider),
            auddConfig = auddConfigMapper.map(input.auddConfig),
            acrCloudConfig = acrCloudConfigMapper.map(input.acrCloudConfig),
            fallbackPolicy = FallbackPolicy(
                noMatches = fallbackActionMapper.map(input.fallbackPolicy.noMatches),
                badConnection = fallbackActionMapper.map(input.fallbackPolicy.badConnection),
                anotherFailure = fallbackActionMapper.map(input.fallbackPolicy.anotherFailure)
            ),
            defaultAudioCaptureMode = audioCaptureModeMapper.map(input.defaultAudioCaptureMode),
            mainButtonLongPressAudioCaptureMode = audioCaptureModeMapper.map(input.mainButtonLongPressAudioCaptureMode),
            hapticFeedback = HapticFeedback(
                vibrateOnTap = input.hapticFeedback.vibrateOnTap,
                vibrateOnResult = input.hapticFeedback.vibrateOnResult
            ),
            useGridForRecognitionQueue = input.useGridForRecognitionQueue,
            showCreationDateInQueue = input.showCreationDateInQueue,
        )
    }
}
