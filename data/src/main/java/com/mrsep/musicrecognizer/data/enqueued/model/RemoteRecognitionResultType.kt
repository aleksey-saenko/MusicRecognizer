package com.mrsep.musicrecognizer.data.enqueued.model

enum class RemoteRecognitionResultType {
    Success,
    NoMatches,
    BadConnection,
    BadRecording,
    WrongToken,
    LimitedToken,
    HttpError,
    UnhandledError
}