package com.mrsep.musicrecognizer.data.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.mrsep.musicrecognizer.di.DefaultDispatcher
import com.mrsep.musicrecognizer.domain.RecordResult
import com.mrsep.musicrecognizer.domain.RecorderController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.IOException
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

private const val TAG = "MediaRecorderController"

@Singleton
class MediaRecorderControllerOld @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : RecorderController {

    private var recorder: MediaRecorder? = null

    private val amplitudePollingEnabled = MutableStateFlow(false)

    override val maxAmplitudeFlow = amplitudePollingEnabled
        .transform { enabled ->
            if (enabled) {
                val fullChunkSize = 5
                // Small amplitude polling rate (like 10 ms) can lead to unstable behaviour
                // (maxAmplitude() sometimes return 0, even if there is some sound around)
                val amplitudePollingRateInMs = 50L
                val chunk = ArrayDeque<Float>(fullChunkSize)
                while (true) {
                    val relativeValue = recorder?.run {
                        (maxAmplitude / 32_768f).coerceIn(0f..1f)
                    } ?: 0f
                    chunk.add(relativeValue)
                    if (chunk.size == fullChunkSize) {
                        val averageValue = (chunk.sum() / fullChunkSize).roundTo(1)
                        emit(averageValue)
                        chunk.removeFirst()
                    }
                    delay(amplitudePollingRateInMs)
                }
            } else {
                emit(0f)
            }
        }
        .distinctUntilChanged { old, new -> old.equalsDelta(new, 0.01f) }
        .flowOn(defaultDispatcher)
        .onEach { println("maxAmplitude=$it") } // debug purpose


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

            recordToFile(file, duration, onFinish, onError)

            continuation.invokeOnCancellation {
                stopRecord()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun recordToFile(
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
                start()
                amplitudePollingEnabled.update { true }
                Log.d(TAG, "recorder started")
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed", e)
                onError(MEDIA_RECORDER_ERROR_PREPARE_FAILED)
            }
        }
    }

    private fun stopRecord() {
        amplitudePollingEnabled.update { false }
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


private fun Float.roundTo(decimalPlaces: Int): Float {
    check(decimalPlaces > 0) { "decimalPlaces must be > 0" }
    val factor = 10f.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor
}

private fun Float.equalsDelta(other: Float, delta: Float) = abs(this - other) < delta