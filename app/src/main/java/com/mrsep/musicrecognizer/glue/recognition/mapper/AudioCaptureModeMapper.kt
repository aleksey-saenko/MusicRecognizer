package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.AudioCaptureModeDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioCaptureMode
import javax.inject.Inject

class AudioCaptureModeMapper @Inject constructor() :
    BidirectionalMapper<AudioCaptureModeDo, AudioCaptureMode> {

    override fun map(input: AudioCaptureModeDo): AudioCaptureMode {
        return when (input) {
            AudioCaptureModeDo.Microphone -> AudioCaptureMode.Microphone
            AudioCaptureModeDo.Device -> AudioCaptureMode.Device
            AudioCaptureModeDo.Auto -> AudioCaptureMode.Auto
        }
    }

    override fun reverseMap(input: AudioCaptureMode): AudioCaptureModeDo {
        return when (input) {
            AudioCaptureMode.Microphone -> AudioCaptureModeDo.Microphone
            AudioCaptureMode.Device -> AudioCaptureModeDo.Device
            AudioCaptureMode.Auto -> AudioCaptureModeDo.Auto
        }
    }
}
