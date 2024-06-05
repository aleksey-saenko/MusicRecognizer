package com.mrsep.musicrecognizer.data.remote

sealed class ConfigValidationStatusDo {

    data object Success : ConfigValidationStatusDo()

    data class Error(val e: RemoteRecognitionResultDo.Error) : ConfigValidationStatusDo()
}
