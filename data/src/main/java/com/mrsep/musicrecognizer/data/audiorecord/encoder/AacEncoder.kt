package com.mrsep.musicrecognizer.data.audiorecord.encoder

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.mrsep.musicrecognizer.data.audiorecord.AudioEncoderDispatcher
import com.mrsep.musicrecognizer.data.audiorecord.AudioEncoderHandler
import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.job
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

private const val TAG = "AacEncoder"

@Singleton
class AacEncoder @Inject constructor(
    private val audioSource: SoundSource
) {

    val aacPacketsFlow: Flow<Result<AacPacket>> = channelFlow<Result<AacPacket>> {
        try {
            val audioSourceParams = checkNotNull(audioSource.params)
            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            coroutineContext.job.invokeOnCompletion {
                codec.stop()
                codec.release()
            }
            val channels = audioSourceParams.audioFormat.channelCount
            val sampleRate = audioSourceParams.audioFormat.sampleRate
            val mediaFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRate,
                channels
            )
            val encodingBitRate = 160 * 1024 // 160 kB/s
            val encoderBufferSize = audioSourceParams.chunkSize
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, encoderBufferSize)
            mediaFormat.setInteger(
                MediaFormat.KEY_PCM_ENCODING,
                audioSourceParams.audioFormat.encoding
            )
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, encodingBitRate)
            mediaFormat.setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            val pcmChunkChannel = audioSource.pcmChunkFlow.transform { pcmChunkResult ->
                pcmChunkResult.onSuccess { pcmChunk ->
                    emit(pcmChunk)
                }.onFailure { cause ->
                    close(cause)
                }
            }.produceIn(this)

            var lastTimestampSec = 0.0

            val inputBufferIdChannel = Channel<Int>(Channel.UNLIMITED)
            inputBufferIdChannel.receiveAsFlow().onEach { bufferId ->
                codec.getInputBuffer(bufferId)?.let { inputBuffer ->
                    val pcmChunk = pcmChunkChannel.receive()
                    inputBuffer.put(pcmChunk)
                    codec.queueInputBuffer(
                        bufferId,
                        0,
                        pcmChunk.size,
                        (lastTimestampSec * 1_000_000).roundToLong(),
                        0
                    )
                    lastTimestampSec += audioSourceParams.chunkSizeInSeconds
                }
            }.launchIn(this)

            val callback = object : MediaCodec.Callback() {
                private val callbackTag = "MediaCodec.Callback"
                override fun onInputBufferAvailable(codec: MediaCodec, bufferId: Int) {
                    if (bufferId >= 0) inputBufferIdChannel.trySendBlocking(bufferId)
                }

                override fun onOutputBufferAvailable(
                    codec: MediaCodec,
                    bufferId: Int,
                    bufferInfo: MediaCodec.BufferInfo
                ) {
                    if (bufferId >= 0 &&
                        ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != MediaCodec.BUFFER_FLAG_CODEC_CONFIG)
                    ) {
                        codec.getOutputBuffer(bufferId)?.let { outputBuffer ->
                            val encodedSize = outputBuffer.remaining()
                            val destination = ByteArray(encodedSize + 7)
                            destination.addAdtsHeader(encodedSize, audioSourceParams.audioFormat)
                            val encodedData = ByteArray(encodedSize)
                            outputBuffer.get(encodedData)
                            encodedData.copyInto(destination, 7)
                            val aacPacket = AacPacket(destination, bufferInfo.presentationTimeUs)
                            trySendBlocking(Result.success(aacPacket))
                        }
                    }
                    codec.releaseOutputBuffer(bufferId, false)
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    throw RuntimeException("Code:${e.errorCode}, message:${e.message}", e.cause)
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    // NO-OP
                }
            }

            codec.setCallback(callback, AudioEncoderHandler)
            codec.start()
        } catch (e: Exception) {
            close(e)
        }
    }
        .catch { cause ->
            Log.e(TAG, "An error occurred during audio encoding", cause)
            emit(Result.failure(cause))
        }
        .flowOn(AudioEncoderDispatcher)

    //accept AAC LC (Low Complexity) only !
    //see https://wiki.multimedia.cx/index.php?title=MPEG-4_Audio
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