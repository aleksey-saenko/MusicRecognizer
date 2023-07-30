package com.mrsep.musicrecognizer.data.remote

sealed class TokenValidationStatusDo {

    data object Success: TokenValidationStatusDo()

    data class Error(val e: RemoteRecognitionResultDo.Error): TokenValidationStatusDo()

}