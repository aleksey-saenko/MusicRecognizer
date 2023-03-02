package com.mrsep.musicrecognizer.data.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.mrsep.musicrecognizer.domain.RecorderController
import com.mrsep.musicrecognizer.domain.RecordResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "MediaRecorderController"

@Singleton
class MediaRecorderController @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : RecorderController {

    private var recorder: MediaRecorder? = null

    override suspend fun recordAudioToFile(file: File, duration: Duration): RecordResult {
        return suspendCancellableCoroutine { continuation ->
            val onFinish = {
                stopRecord()
                continuation.resume(RecordResult.Success)
            }
            val onError = { errorCode: Int ->
                val errorResult = when (errorCode) {
                    MediaRecorder.MEDIA_ERROR_SERVER_DIED -> {
                        RecordResult.Error.ServerDied
                    }
                    MEDIA_RECORDER_ERROR_PREPARE_FAILED -> {
                        RecordResult.Error.PrepareFailed
                    }
                    else -> {
                        RecordResult.Error.UnhandledError(
                            throwable = RuntimeException("MEDIA_RECORDER_ERROR(code:$errorCode)")
                        )
                    }
                }
                stopRecord()
                continuation.resume(errorResult)
            }

            startRecordToFile(file, duration, onFinish, onError)

            continuation.invokeOnCancellation { stopRecord() }
        }
    }

    @Suppress("DEPRECATION")
    private fun startRecordToFile(
        file: File,
        duration: Duration,
        onFinish: () -> Unit,
        onError: (errorCode: Int) -> Unit
    ) {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }.apply {
//            setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOnInfoListener { _, what, extra ->
                Log.d(TAG, "InfoListener: what=$what, extra=$extra")
                when (what) {
                    MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                        onFinish()
                    }
                }
            }
            setOnErrorListener { _, what, extra ->
                Log.d(TAG, "ErrorListener: what=$what, extra=$extra")
                onError(what)
            }
            setMaxDuration(duration.toMillis().toInt())
//            setPreferredMicrophoneDirection(MicrophoneDirection.MIC_DIRECTION_AWAY_FROM_USER)
//            setPreferredMicrophoneDirection(MicrophoneDirection.MIC_DIRECTION_TOWARDS_USER)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed", e)
                onError(MEDIA_RECORDER_ERROR_PREPARE_FAILED)
            }
            start()
            Log.d(TAG, "recorder started")
        }
    }

    private fun stopRecord() {
        recorder?.apply {
            try {
                stop()
            } catch (e: RuntimeException) {
                Log.e(TAG, "too early stop, output file is corrupted", e)
            }
            release()
        }
        recorder = null
        Log.d(TAG, "recorder stopped")
    }

    companion object {
        private const val MEDIA_RECORDER_ERROR_PREPARE_FAILED = -1
    }

}