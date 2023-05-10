package com.mrsep.musicrecognizer.data.audiorecord

import kotlinx.coroutines.flow.Flow

interface SoundAmplitudeSource {

    val amplitudeFlow: Flow<Float>

}