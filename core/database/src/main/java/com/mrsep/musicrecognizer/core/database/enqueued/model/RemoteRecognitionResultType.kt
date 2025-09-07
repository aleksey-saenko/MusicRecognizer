package com.mrsep.musicrecognizer.core.database.enqueued.model

enum class RemoteRecognitionResultType {
    Success,
    NoMatches,
    BadConnection,
    BadRecording,
    AuthError,
    ApiUsageLimited,
    HttpError,
    UnhandledError
}
