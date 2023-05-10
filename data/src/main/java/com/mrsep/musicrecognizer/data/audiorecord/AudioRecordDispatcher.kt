package com.mrsep.musicrecognizer.data.audiorecord

import android.os.Handler
import android.os.Process
import android.os.HandlerThread
import kotlinx.coroutines.android.asCoroutineDispatcher

/**
 * CoroutineDispatcher for audio recording purposes. It uses a dedicated thread
 * and elevates its priority to [android.os.Process.THREAD_PRIORITY_URGENT_AUDIO].
 */

val AudioRecordHandler = HandlerThread(
    "myRecordThread",
    Process.THREAD_PRIORITY_URGENT_AUDIO
)
    .apply { start() }
    .run { Handler(this.looper) }
val AudioRecordDispatcher = AudioRecordHandler.asCoroutineDispatcher()


val AudioEncoderHandler = HandlerThread(
    "myEncoderThread",
    Process.THREAD_PRIORITY_URGENT_AUDIO
)
    .apply { start() }
    .run { Handler(this.looper) }
val AudioEncoderDispatcher = AudioEncoderHandler.asCoroutineDispatcher()
