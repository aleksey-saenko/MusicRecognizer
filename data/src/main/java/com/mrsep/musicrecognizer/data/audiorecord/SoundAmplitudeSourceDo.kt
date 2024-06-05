package com.mrsep.musicrecognizer.data.audiorecord

import kotlinx.coroutines.flow.Flow

interface SoundAmplitudeSourceDo {

    val amplitudeFlow: Flow<Float>
}
