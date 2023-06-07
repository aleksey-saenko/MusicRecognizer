package com.mrsep.musicrecognizer.data.remote

sealed class TokenValidationStatusDo {

    object Success: TokenValidationStatusDo()

    data class Error(val e: RemoteRecognitionResultDo.Error): TokenValidationStatusDo()

}