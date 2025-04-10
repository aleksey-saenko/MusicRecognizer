package com.mrsep.musicrecognizer.core.audio.audiorecord.encoder

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioEncoderDispatcher
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioEncoderHandler
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.math.ceil
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds

internal class AdtsRecordingController(
    private val soundSource: SoundSource,
) : AudioRecordingController {

    override val soundLevel = soundSource.soundLevel

    override fun audioRecordingFlow(scheme: RecordingScheme) = channelFlow {
        var codecRef: MediaCodec? = null
        try {
            val soundSourceParams = checkNotNull(soundSource.params)
            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            codecRef = codec
            // MediaFormat will be updated once after start on receive codec-specific data (csd-0)
            var mediaFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                soundSourceParams.audioFormat.sampleRate,
                soundSourceParams.audioFormat.channelCount
            ).apply {
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, soundSourceParams.chunkSize)
                setInteger(MediaFormat.KEY_PCM_ENCODING, soundSourceParams.audioFormat.encoding)
                setInteger(MediaFormat.KEY_BIT_RATE, 160 * 1024) // 160 kbit/s
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            }
            val aacFrameDuration = aacLcFrameDuration(soundSourceParams.audioFormat.sampleRate)

            val silenceTracker = UnsafeSilenceTracker()
            launch {
                soundSource.soundLevel
                    .map { it == 0f }
                    .distinctUntilChanged()
                    .collect(silenceTracker::onSilenceStateChanged)
            }

            lateinit var recorderStartTimestamp: Instant
            var isStartTimestampUpdated = false
            val pcmChunkChannel = soundSource.pcmChunkFlow
                .transform { pcmChunkResult ->
                    pcmChunkResult
                        .onSuccess { pcmChunk ->
                            if (!isStartTimestampUpdated) {
                                recorderStartTimestamp = Instant.now()
                                    .minusMillis((soundSourceParams.chunkSizeInSeconds * 1_000).toLong())
                                isStartTimestampUpdated = true
                            }
                            emit(pcmChunk)
                        }
                        .onFailure { cause -> close(cause) }
                }
                .buffer(Channel.UNLIMITED)
                .produceIn(this)

            val inputBufferIdChannel = Channel<Int>(Channel.UNLIMITED)
            var nextPresentationTimeSec = 0.0
            inputBufferIdChannel.receiveAsFlow().onEach { bufferId ->
                codec.getInputBuffer(bufferId)?.let { inputBuffer ->
                    val pcmChunk = pcmChunkChannel.receive()
                    inputBuffer.put(pcmChunk)
                    codec.queueInputBuffer(
                        bufferId,
                        0,
                        pcmChunk.size,
                        (nextPresentationTimeSec * 1_000_000).roundToLong(),
                        0
                    )
                    nextPresentationTimeSec += soundSourceParams.chunkSizeInSeconds
                }
            }.launchIn(this)

            val callback = object : MediaCodec.Callback() {

                val emissions = scheme.toRecordingEmissions(aacFrameDuration)
                var emittedRecordingCount = 0

                override fun onInputBufferAvailable(codec: MediaCodec, bufferId: Int) {
                    if (bufferId >= 0) inputBufferIdChannel.trySendBlocking(bufferId)
                }

                override fun onOutputBufferAvailable(
                    codec: MediaCodec,
                    bufferId: Int,
                    bufferInfo: MediaCodec.BufferInfo,
                ) {
                    // Skip any non-data buffers, we already get csd from onOutputFormatChanged()
                    if (bufferId < 0 || bufferInfo.flags != 0) {
                        codec.releaseOutputBuffer(bufferId, false)
                        return
                    }
                    val outputBuffer = codec.getOutputBuffer(bufferId) ?: return
                    val adtsPacket = ByteArray(bufferInfo.size + 7).apply {
                        addAdtsHeader(bufferInfo.size, soundSourceParams.audioFormat)
                        outputBuffer.get(this, 7, bufferInfo.size)
                    }
                    val bufferPresentationTimeStart = bufferInfo.presentationTimeUs.microseconds
                    val bufferPresentationTimeEnd = bufferPresentationTimeStart + aacFrameDuration
                    emissions.forEach { emission ->
                        if (bufferPresentationTimeStart < emission.presentationOffset || emission.isBufferReleased) return@forEach
                        emission.addFrameToBuffer(adtsPacket)
                        if (emission.hasRequiredBufferedDuration) {
                            val data = emission.getBufferData()
                            val emissionDuration = emission.bufferedDuration
                            val emissionPresentationTimeStart = bufferPresentationTimeEnd - emissionDuration
                            val emissionStartTimestamp = recorderStartTimestamp
                                .plusMillis(emissionPresentationTimeStart.inWholeMilliseconds)
                            val silenceDuration = silenceTracker.querySilenceDuration(
                                startTime = emissionStartTimestamp,
                                endTime = emissionStartTimestamp.plusMillis(emissionDuration.inWholeMilliseconds)
                            )
                            val result = AudioRecording(
                                data = data,
                                duration = emissionDuration,
                                nonSilenceDuration = emissionDuration - silenceDuration,
                                startTimestamp = emissionStartTimestamp,
                                isFallback = emission.isFallback,
                            )
                            trySendBlocking(Result.success(result))
                            emittedRecordingCount++
                            emission.releaseBuffer()
                        }
                    }
                    codec.releaseOutputBuffer(bufferId, false)
                    if (emissions.size == emittedRecordingCount) close()
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    close(e)
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    // Should only happen once after starting when set codec-specific data (csd-0)
                    // We don't use it though
                    mediaFormat = format
                }
            }
            codec.setCallback(callback, AudioEncoderHandler)
            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//            checkFloatEncodingSupport()
            codec.start()
            awaitCancellation()
        } catch (e: Exception) {
            close(e)
        } finally {
            codecRef?.stop()
            codecRef?.release()
        }
    }
        .catch { cause -> emit(Result.failure(cause)) }
        .flowOn(AudioEncoderDispatcher)

    // This check is recommended by the documentation.
    // But it turned out that sometimes the codec works well, although the check shows no support.
    @Suppress("unused")
    private fun checkFloatEncodingSupport(sourceFormat: AudioFormat, codecFormat: MediaFormat) {
        if (sourceFormat.encoding == AudioFormat.ENCODING_PCM_FLOAT) {
            val isCodecConfiguredWithPcmFloat = try {
                codecFormat.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT
            } catch (e: NullPointerException) {
                false
            }
            check(isCodecConfiguredWithPcmFloat) {
                "Configured mediaCodec doesn't support float PCM encoding on this device"
            }
        }
    }

    // This method is implemented only for AAC-LC
    // AAC-LC: each AAC frame is 1024 samples per channel
    private fun aacLcFrameDuration(sampleRate: Int): Duration = (1024.0 / sampleRate).seconds

    // This method is implemented only for AAC-LC
    // See https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio
    private fun ByteArray.addAdtsHeader(length: Int, audioFormat: AudioFormat) {
        val packetLen = length + 7
        require(this.size == packetLen)
        val headerFrequencyType = mapOf(
            96000 to 0,
            88200 to 1,
            64000 to 2,
            48000 to 3,
            44100 to 4,
            32000 to 5,
            24000 to 6,
            22050 to 7,
            16000 to 8,
            12000 to 9,
            11025 to 10,
            8000 to 11,
            7350 to 12
        )
        val sampleRateType = headerFrequencyType.getOrDefault(audioFormat.sampleRate, 15)
        val channels = audioFormat.channelCount
        val profile = 2 // AAC LC (Low Complexity)

        this[0] = 0xFF.toByte()
        this[1] = 0xF9.toByte()
        this[2] = ((profile - 1 shl 6) + (sampleRateType shl 2) + (channels shr 2)).toByte()
        this[3] = ((channels and 3 shl 6) + (packetLen shr 11)).toByte()
        this[4] = (packetLen and 0x7FF shr 3).toByte()
        this[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        this[6] = 0xFC.toByte()
    }
}

private class RecordingEmission(
    val presentationOffset: Duration,
    val minDuration: Duration,
    val isFallback: Boolean,
    val frameDuration: Duration,
) {
    var bufferedDuration = Duration.ZERO
        get() {
            if (isBufferReleased) error("Buffer is already released")
            return buffer?.let { field } ?: Duration.ZERO
        }
        private set
    var isBufferReleased: Boolean = false
        private set
    val hasRequiredBufferedDuration get() = bufferedDuration >= minDuration
    private var buffer: MutableList<ByteArray>? = null

    fun addFrameToBuffer(frame: ByteArray) {
        if (isBufferReleased) error("Buffer is already released")
        if (buffer == null) {
            buffer = ArrayList(ceil(minDuration / frameDuration).toInt() + 2)
        }
        buffer!!.add(frame)
        bufferedDuration += frameDuration
    }

    fun getBufferData(): ByteArray {
        if (isBufferReleased) error("Buffer is already released")
        return buffer?.concat() ?: ByteArray(0)
    }

    fun releaseBuffer() {
        buffer = null
        isBufferReleased = true
    }

    private fun List<ByteArray>.concat(): ByteArray {
        return ByteArray(sumOf { it.size }).also { result ->
            var offset = 0
            forEach { bytes ->
                bytes.copyInto(result, offset)
                offset += bytes.size
            }
        }
    }
}

private fun RecordingScheme.toRecordingEmissions(frameDuration: Duration) = buildList {
    fallback?.let { fallbackDuration ->
        val fallbackEmission = RecordingEmission(
            presentationOffset = Duration.ZERO,
            minDuration = fallbackDuration,
            isFallback = true,
            frameDuration
        )
        add(fallbackEmission)
    }
    var stepOffset = Duration.ZERO
    steps.forEach { step ->
        step.recordings.forEach { recordingDuration ->
            val emission = RecordingEmission(
                presentationOffset = stepOffset,
                minDuration = recordingDuration,
                isFallback = false,
                frameDuration
            )
            add(emission)
        }.also {
            stepOffset += step.recordings.last()
        }
    }
}
