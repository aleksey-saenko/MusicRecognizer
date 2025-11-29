package com.mrsep.musicrecognizer.core.audio.audiorecord.decoder

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaCodecInfo.CodecCapabilities.FEATURE_MultipleFrames
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.MediaExtractorCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.ArrayDeque

private const val TAG = "AudioDecoder"

@OptIn(UnstableApi::class)
class AudioDecoder(private val appContext: Context) {

    suspend fun decode(input: File): Result<DecodedAudio> = withContext(Dispatchers.Default) {
        val extractor = MediaExtractorCompat(appContext)
        var codecRef: MediaCodec? = null
        var handlerThreadRef: HandlerThread? = null
        try {
            extractor.setDataSource(input.absolutePath)
            var trackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mimeType = format.getString(MediaFormat.KEY_MIME)
                if (MimeTypes.isAudio(mimeType)) {
                    trackIndex = i
                    break
                }
            }
            require(trackIndex >= 0) { "No audio track found" }
            extractor.selectTrack(trackIndex)
            val inputFormat = extractor.getTrackFormat(trackIndex)
            val inputMimeType = inputFormat.getString(MediaFormat.KEY_MIME)!!

            // Skip decoding if audio is already raw
            if (inputMimeType == MediaFormat.MIMETYPE_AUDIO_RAW) {
                val rawOutputChunks = mutableListOf<ByteBuffer>()
                var rawOutputChunksByteSize = 0
                do {
                    ensureActive()
                    val sampleSize = extractor.sampleSize.toInt()
                    val sampleBuffer = ByteBuffer.allocate(sampleSize)
                    extractor.readSampleData(sampleBuffer,  /* offset= */0)
                    rawOutputChunks.add(sampleBuffer)
                    rawOutputChunksByteSize += sampleSize
                } while (extractor.advance())

                val result = ByteArray(rawOutputChunksByteSize)
                var offset = 0
                for (chunk in rawOutputChunks) {
                    val bufferSize = chunk.remaining()
                    chunk.get(result, offset, bufferSize)
                    offset += bufferSize
                }
                return@withContext Result.success(DecodedAudio(
                    data = result,
                    channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT),
                    sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                    pcmEncoding = inputFormat.getInteger(MediaFormat.KEY_PCM_ENCODING),
                ))
            }

            val codec = MediaCodec.createDecoderByType(inputMimeType)
            codecRef = codec

            // Try to configure mediaCodec in batching mode to speedup decoding
            var isBatchingMode = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                isBatchingMode = codec.codecInfo
                    .getCapabilitiesForType(inputMimeType)
                    .isFeatureSupported(FEATURE_MultipleFrames)
                if (isBatchingMode) {
                    inputFormat.apply {
                        setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BATCH_MODE_MAX_INPUT_SIZE)
                        setInteger(MediaFormat.KEY_BUFFER_BATCH_MAX_OUTPUT_SIZE, BATCH_MODE_MAX_OUTPUT_SIZE)
                        setInteger(MediaFormat.KEY_BUFFER_BATCH_THRESHOLD_OUTPUT_SIZE, BATCH_MODE_MAX_OUTPUT_SIZE)
                    }
                }
            }
            Log.d(TAG, "MediaCodec will be configured in batching mode")

            val handlerThread = HandlerThread("audio-decoder").apply {
                start()
                handlerThreadRef = this
            }
            val handler = Handler(handlerThread.looper)

            val outputChunks = mutableListOf<ByteArray>()
            var outputChunksByteSize = 0
            val completion = CompletableDeferred<Unit>()

            codec.setCallback(object : MediaCodec.Callback() {

                var sawInputEOS = false
                var sentInputEOS = false

                override fun onInputBufferAvailable(codec: MediaCodec, bufferId: Int) {
                    if (sentInputEOS || bufferId < 0) return
                    val inputBuffer = codec.getInputBuffer(bufferId) ?: return

                    if (sawInputEOS && !sentInputEOS) {
                        codec.queueInputBuffer(bufferId, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        sentInputEOS = true
                    }
                    if (sentInputEOS) return

                    if (isBatchingMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        var offset = 0
                        val infos = ArrayDeque<BufferInfo>()
                        while (!sawInputEOS) {
                            if (inputBuffer.position() != 0 &&
                                extractor.sampleSize > inputBuffer.capacity() - inputBuffer.limit()) {
                                // This sample will be placed in the next buffer
                                break
                            }
                            val sampleSize = extractor.readSampleData(inputBuffer, offset)
                            val info = BufferInfo().apply {
                                this.offset = offset
                                this.size = sampleSize
                                this.presentationTimeUs = extractor.sampleTime
                                this.flags = extractor.sampleFlags
                            }
                            infos.addLast(info)
                            offset += sampleSize
                            sawInputEOS = extractor.advance().not()
                        }
                        inputBuffer.position(0)
                        codec.queueInputBuffers(bufferId, infos)
                    } else {
                        val sampleSize = extractor.readSampleData(inputBuffer,  /* offset= */0)
                        codec.queueInputBuffer(bufferId, 0, sampleSize, extractor.sampleTime, extractor.sampleFlags)
                        sawInputEOS = extractor.advance().not()
                    }
                }

                // Some codecs like audio/mpeg sends BUFFER_FLAG_END_OF_STREAM here
                override fun onOutputBuffersAvailable(
                    codec: MediaCodec,
                    bufferId: Int,
                    infos: ArrayDeque<BufferInfo>
                ) {
                    if (bufferId < 0) return
                    if (infos.any { it.size > 0 }) {
                        val outputBuffer = codec.getOutputBuffer(bufferId) ?: return
                        val chunk = ByteArray(outputBuffer.remaining())
                        outputBuffer.get(chunk)
                        outputChunks.add(chunk)
                        outputChunksByteSize += chunk.size
                    }
                    codec.releaseOutputBuffer(bufferId, false)
                    if (infos.any { it.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0 }) {
                        completion.complete(Unit)
                    }
                }

                // Some codecs like audio/aac sends BUFFER_FLAG_END_OF_STREAM here
                override fun onOutputBufferAvailable(codec: MediaCodec, bufferId: Int, info: BufferInfo) {
                    if (bufferId < 0) return
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        codec.releaseOutputBuffer(bufferId, false)
                        completion.complete(Unit)
                        return
                    }
                    if (info.size > 0) {
                        val outputBuffer = codec.getOutputBuffer(bufferId) ?: return
                        val chunk = ByteArray(outputBuffer.remaining()).apply { outputBuffer.get(this) }
                        outputChunks.add(chunk)
                        outputChunksByteSize += chunk.size
                    }
                    codec.releaseOutputBuffer(bufferId, false)
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    Log.d(TAG, "onOutputFormatChanged: format = $format")
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    completion.completeExceptionally(e)
                }
            }, handler)

            codec.configure(inputFormat, null, null, 0)
            codec.start()
            completion.await()

            val result = if (outputChunks.size == 1) {
                outputChunks[0]
            } else {
                ByteArray(outputChunksByteSize).also {
                    var dest = 0
                    for (chunk in outputChunks) {
                        System.arraycopy(chunk, 0, it, dest, chunk.size)
                        dest += chunk.size
                    }
                }
            }

            val outputFormat = codec.outputFormat
            Result.success(DecodedAudio(
                data = result,
                channelCount = outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT),
                sampleRate = outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                pcmEncoding = try {
                    outputFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
                } catch (_: NullPointerException) {
                    AudioFormat.ENCODING_PCM_16BIT // Default, see docs
                }
            ))
        } catch (e: Exception) {
            ensureActive()
            Result.failure(e)
        } finally {
            codecRef?.stop()
            codecRef?.release()
            extractor.release()
            handlerThreadRef?.quit()
        }
    }

    companion object {
        private const val BATCH_MODE_MAX_INPUT_SIZE = 1024 * 1024 // 1 MB
        private const val BATCH_MODE_MAX_OUTPUT_SIZE = 2 * 1024 * 1024 // 2 MB
    }
}
