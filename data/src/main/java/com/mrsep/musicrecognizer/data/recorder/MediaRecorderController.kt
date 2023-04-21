package com.mrsep.musicrecognizer.data.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.IOException
import java.time.Duration
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayDeque
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

private const val TAG = "MediaRecorderController"
private const val FILE_EXTENSION = "m4a"

@OptIn(ExperimentalCoroutinesApi::class)
class MediaRecorderController @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val fileRecordRepository: FileRecordDataRepository,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : RecorderDataController {

    private var recorder: MediaRecorder? = null

    private val amplitudePollingEnabled = MutableStateFlow(false)

    private val maxAmplitudeRawFlow = flow {
        while (true) {
            // 32_767 is the maximum output value
            val relativeValue = try {
                recorder?.maxAmplitude?.div(32_767f)?.coerceIn(0f..1f) ?: 0f
            } catch (e: IllegalStateException) {
                0f
            }
            emit(relativeValue)
            // Small amplitude polling rate (like 10 ms) can lead to unstable behaviour
            // (maxAmplitude() sometimes return 0, even if there is some sound around)
            delay(50L)
        }
    }

    override val maxAmplitudeFlow = amplitudePollingEnabled
        .flatMapLatest { enabled ->
            if (enabled) {
                maxAmplitudeRawFlow
                    .transformToMovingAverage(5)
                    .map { average -> average.roundTo(1) }
            } else {
                flow { emit(0f) }
            }
        }
        .distinctUntilChanged { old, new -> old.equalsDelta(new, 0.01f) }
        .flowOn(defaultDispatcher)
        .onEach { println("maxAmplitude=$it") } // debug purpose


    private fun Flow<Float>.transformToMovingAverage(windowSize: Int) = flow {
        val chunk = ArrayDeque<Float>(windowSize)
        collect { maxAmplitude ->
            chunk.add(maxAmplitude)
            if (chunk.size == windowSize) {
                emit(chunk.sum() / windowSize)
                chunk.removeFirst()
            }
        }
    }

    override suspend fun recordAudioToFile(duration: Duration): RecordDataResult {
        return suspendCancellableCoroutine { continuation ->
            val resultFile = fileRecordRepository.getFileForNewRecord(FILE_EXTENSION)

            val onFinish = {
                stopRecord()
                continuation.resume(RecordDataResult.Success(resultFile))
            }
            val onError = { throwable: Throwable ->
                stopRecord()
                fileRecordRepository.delete(resultFile)
                continuation.resume(RecordDataResult.Error(throwable))
            }

            recordToFile(resultFile, duration, onFinish, onError)

            continuation.invokeOnCancellation {
                stopRecord()
                fileRecordRepository.delete(resultFile)
            }
        }
    }

    //FIXME add stop-release to prev recorder (see PlayerController)
    private fun recordToFile(
        file: File,
        duration: Duration,
        onFinish: () -> Unit,
        onError: (throwable: Throwable) -> Unit
    ) {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(appContext)
        } else {
            @Suppress("DEPRECATION")
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
                onError(RuntimeException("Media recorder error, code:$what, extra=$extra"))
            }
            setMaxDuration(duration.toMillis().toInt())
//            setPreferredMicrophoneDirection(MicrophoneDirection.MIC_DIRECTION_AWAY_FROM_USER)
//            setPreferredMicrophoneDirection(MicrophoneDirection.MIC_DIRECTION_TOWARDS_USER)
            try {
                prepare()
                start()
                amplitudePollingEnabled.update { true }
                Log.d(TAG, "Recorder started")
            } catch (e: IOException) {
                Log.e(TAG, "Recorder prepare fails", e)
                onError(e)
            }
        }
    }

    private fun stopRecord() {
        amplitudePollingEnabled.update { false }
        recorder?.apply {
            try {
                stop()
            } catch (e: RuntimeException) {
                Log.w(TAG, "Too early stop, the output file is corrupted", e)
            }
            release()
        }
        recorder = null
        Log.d(TAG, "Recorder stopped")
    }


}


private fun Float.roundTo(decimalPlaces: Int): Float {
    check(decimalPlaces > 0) { "decimalPlaces must be > 0" }
    val factor = 10f.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor
}

private fun Float.equalsDelta(other: Float, delta: Float) = abs(this - other) < delta