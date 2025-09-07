package com.mrsep.musicrecognizer.core.audio.audiorecord

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import kotlinx.coroutines.android.asCoroutineDispatcher

/**
 * CoroutineDispatcher for audio recording purposes. It uses a dedicated thread
 * and elevates its priority to [android.os.Process.THREAD_PRIORITY_URGENT_AUDIO].
 */

internal val AudioRecordHandler = HandlerThread(
    "myRecordThread",
    Process.THREAD_PRIORITY_URGENT_AUDIO
)
    .apply { start() }
    .run { Handler(this.looper) }
internal val AudioRecordDispatcher = AudioRecordHandler.asCoroutineDispatcher()

internal val AudioEncoderHandler = HandlerThread(
    "myEncoderThread",
    Process.THREAD_PRIORITY_URGENT_AUDIO
)
    .apply { start() }
    .run { Handler(this.looper) }
internal val AudioEncoderDispatcher = AudioEncoderHandler.asCoroutineDispatcher()
