package com.mrsep.musicrecognizer.core.domain.recognition.model

sealed class RecognitionStatus {

    data object Ready : RecognitionStatus()

    data class Recognizing(val extraTime: Boolean) : RecognitionStatus()

    data class Done(val result: RecognitionResult) : RecognitionStatus()
}
