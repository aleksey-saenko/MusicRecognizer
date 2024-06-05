package com.mrsep.musicrecognizer.data.remote

interface ConfigValidatorDo {

    suspend fun validate(config: RecognitionServiceConfigDo): ConfigValidationStatusDo
}
