package com.mrsep.musicrecognizer.data.remote.enhancer

import com.mrsep.musicrecognizer.data.track.TrackEntity

sealed class RemoteMetadataEnhancingResultDo {

    data class Success(val track: TrackEntity) : RemoteMetadataEnhancingResultDo()

    data object NoEnhancement : RemoteMetadataEnhancingResultDo()

    sealed class Error : RemoteMetadataEnhancingResultDo() {

        data object BadConnection : Error()

        data class HttpError(
            val code: Int,
            val message: String
        ) : Error()

        data class UnhandledError(
            val message: String = "",
            val cause: Throwable? = null
        ) : Error()
    }
}
