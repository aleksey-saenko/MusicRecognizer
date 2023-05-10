package com.mrsep.musicrecognizer.data.remote.audd.model

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult

sealed class TokenValidationDataStatus {

    object Success: TokenValidationDataStatus()

    data class Error(var e: RemoteRecognitionDataResult.Error): TokenValidationDataStatus()

}