package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import android.util.Log
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioRecordDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.yield
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "DefaultSoundSource"

internal class DefaultSoundSource(
    private val appContext: Context,
    private val mediaProjection: MediaProjection? = null,
) : SoundSource {

    override val audioSource = if (mediaProjection == null) AudioSource.MIC else AudioSource.DEVICE
    override val params: SoundSourceConfig? = SoundSourceConfigProvider.config

    private val soundLevelMeter = SoundLevelMeter(params)
    override val soundLevel by soundLevelMeter::soundLevel

    override val pcmChunkFlow: SharedFlow<Result<ByteArray>> = flow {
        val params = checkNotNull(params) {
            "$TAG: No available configuration for audio recording"
        }
        var audioRecordRef: AudioRecord? = null
        try {
            val oneSecBuffer = params.audioFormat.sampleRate * params.bytesPerFrame
            val realBuffer = (params.minBufferSize * 10).coerceAtLeast(oneSecBuffer)
            val audioRecordBuilder = AudioRecord.Builder()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioRecordBuilder.setContext(appContext)
            }
            if (mediaProjection == null) {
                audioRecordBuilder.setAudioSource(DEFAULT_AUDIO_SOURCE)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val captureConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .addMatchingUsage(AudioAttributes.USAGE_GAME)
                        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                        .build()
                    audioRecordBuilder.setAudioPlaybackCaptureConfig(captureConfig)
                } else {
                    error("AudioPlaybackCapture API is available on Android 10+")
                }
            }
            @SuppressLint("MissingPermission")
            val audioRecord = audioRecordBuilder
                .setAudioFormat(params.audioFormat)
                .setBufferSizeInBytes(realBuffer)
                .build()
            audioRecordRef = audioRecord // False-positive lint warning, don't delete this line
            audioRecord.startRecording()
            check(audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                "AudioRecord cant start recording, is audio input captured by another app?"
            }
            while (true) {
                val pcmChunk = when (params.audioFormat.encoding) {
                    AudioFormat.ENCODING_PCM_16BIT -> {
                        ByteArray(params.chunkSize).apply {
                            audioRecord.read(
                                this,
                                0,
                                this.size,
                                AudioRecord.READ_BLOCKING
                            )
                            soundLevelMeter.processNewChunk(this.toShortArray())
                        }
                    }

                    AudioFormat.ENCODING_PCM_FLOAT -> {
                        FloatArray(params.chunkSize / Float.SIZE_BYTES).apply {
                            audioRecord.read(
                                this,
                                0,
                                this.size,
                                AudioRecord.READ_BLOCKING
                            )
                            soundLevelMeter.processNewChunk(this)
                        }.toByteArray()
                    }

                    else -> error("Unsupported encoding")
                }
                emit(Result.success(pcmChunk))
                yield()
            }
        } finally {
            audioRecordRef?.release()
        }
    }
        .catch { cause ->
            Log.e(TAG, "Error during audio recording", cause)
            emit(Result.failure(cause))
        }
        .shareIn(
            scope = CoroutineScope(AudioRecordDispatcher + SupervisorJob()),
            started = SharingStarted.WhileSubscribed(RECORDER_SHARING_TIMEOUT_MS),
            replay = 0
        )


    companion object {
        private const val DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        private const val RECORDER_SHARING_TIMEOUT_MS = 0L
    }
}

internal fun FloatArray.toByteArray(): ByteArray {
    val byteBuffer = ByteBuffer
        .allocate(this.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
    byteBuffer.asFloatBuffer().put(this)
    return byteBuffer.array()
}

internal fun ByteArray.toShortArray(): ShortArray {
    val output = ShortArray(size.div(Short.SIZE_BYTES))
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.nativeOrder()).asShortBuffer()
    buffer.get(output)
    return output
}
