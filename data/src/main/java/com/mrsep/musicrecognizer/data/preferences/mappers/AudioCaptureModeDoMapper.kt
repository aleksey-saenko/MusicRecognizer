package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.AudioCaptureModeProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.AudioCaptureModeDo
import javax.inject.Inject

internal class AudioCaptureModeDoMapper @Inject constructor() :
    BidirectionalMapper<AudioCaptureModeProto, AudioCaptureModeDo> {

    override fun map(input: AudioCaptureModeProto): AudioCaptureModeDo = when (input) {
        AudioCaptureModeProto.UNRECOGNIZED,
        AudioCaptureModeProto.Unspecified,
        AudioCaptureModeProto.Microphone -> AudioCaptureModeDo.Microphone
        AudioCaptureModeProto.Device -> AudioCaptureModeDo.Device
        AudioCaptureModeProto.Auto -> AudioCaptureModeDo.Auto
    }

    override fun reverseMap(input: AudioCaptureModeDo): AudioCaptureModeProto = when (input) {
        AudioCaptureModeDo.Microphone -> AudioCaptureModeProto.Microphone
        AudioCaptureModeDo.Device -> AudioCaptureModeProto.Device
        AudioCaptureModeDo.Auto -> AudioCaptureModeProto.Auto
    }
}
