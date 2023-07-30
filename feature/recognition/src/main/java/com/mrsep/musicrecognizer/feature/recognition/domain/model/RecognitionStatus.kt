package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RecognitionStatus {

    data object Ready : RecognitionStatus()

    //can be replaced by value class
    data class Recognizing(val extraTry: Boolean) : RecognitionStatus()

    data class Done(val result: RecognitionResult) : RecognitionStatus()


}