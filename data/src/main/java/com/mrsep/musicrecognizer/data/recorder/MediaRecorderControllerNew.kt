package com.mrsep.musicrecognizer.data.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.IOException
import java.time.Duration
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

private const val TAG = "MediaRecorderController"
private const val FILE_EXTENSION = "m4a"

class MediaRecorderControllerNew @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val fileRecordRepository: FileRecordDataRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : RecorderDataController {

    private var recorder: MediaRecorder? = null

    private val amplitudeChannel = Channel<Float>(RENDEZVOUS)
    override val maxAmplitudeFlow = amplitudeChannel.receiveAsFlow()
        .onEach { println("maxAmplitude=$it") } // debug purpose

    private fun CoroutineScope.getLazyAmplitudePollingJob(): Job {
        return launch(defaultDispatcher, CoroutineStart.LAZY) {
            val fullChunkSize = 5
            // Small amplitude polling rate (like 10 ms) can lead to unstable behaviour
            // (maxAmplitude() sometimes return 0, even if there is some sound around)
            val amplitudePollingRateInMs = 50L
            val chunk = ArrayDeque<Float>(fullChunkSize)
            var previousSent = 0f
            try {
                while (true) {
                    val relativeValue = recorder?.run {
                        (maxAmplitude / 32_768f).coerceIn(0f..1f)
                    } ?: 0f
                    chunk.add(relativeValue)
                    if (chunk.size == fullChunkSize) {
                        val averageValue = (chunk.sum() / fullChunkSize).roundTo(1)
                        if (averageValue.equalsDelta(previousSent, 0.01f).not()) {
                            amplitudeChannel.send(averageValue)
                            previousSent = averageValue
                        }
                        chunk.removeFirst()
                    }
                    delay(amplitudePollingRateInMs)
                }
            } finally {
                withContext(NonCancellable) { amplitudeChannel.send(0f) }
            }
        }
    }

    override suspend fun recordAudioToFile(duration: Duration): RecordDataResult {
        return coroutineScope {
            val lazyAmplitudePollingJob = getLazyAmplitudePollingJob()

            suspendCancellableCoroutine { continuation ->
                val resultFile = fileRecordRepository.getFileForNewRecord(FILE_EXTENSION)

                val onStart = {
                    lazyAmplitudePollingJob.start()
                    Unit
                }
                val onFinish = {
                    stopRecord()
                    lazyAmplitudePollingJob.cancel()
                    continuation.resume(RecordDataResult.Success(resultFile))
                }
                val onError = { throwable: Throwable ->
                    stopRecord()
                    lazyAmplitudePollingJob.cancel()
                    fileRecordRepository.delete(resultFile)
                    continuation.resume(RecordDataResult.Error(throwable))
                }

                recordToFile(resultFile, duration, onStart, onFinish, onError)


                continuation.invokeOnCancellation {
                    stopRecord()
                    fileRecordRepository.delete(resultFile)
                }
            }
        }
    }


    private fun recordToFile(
        file: File,
        duration: Duration,
        onStart: () -> Unit,
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
                onStart()
                Log.d(TAG, "Recorder started")
            } catch (e: IOException) {
                Log.e(TAG, "Recorder prepare fails", e)
                onError(e)
            }
        }
    }

    private fun stopRecord() {
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