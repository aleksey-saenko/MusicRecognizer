package com.mrsep.musicrecognizer.core.audio.audiorecord.encoder

import android.content.Context
import android.media.MediaFormat
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.MediaFormatUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.getBufferFlagsFromMediaCodecFlags
import androidx.media3.container.Mp4TimestampData
import androidx.media3.container.Mp4TimestampData.unixTimeToMp4TimeSeconds
import androidx.media3.exoplayer.MediaExtractorCompat
import androidx.media3.muxer.BufferInfo
import androidx.media3.muxer.Mp4Muxer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.time.Instant

class AdtsToMp4Migration (private val appContext: Context) {

    @OptIn(UnstableApi::class)
    suspend fun convert(
        input: File, output: File, creationTimestamp: Instant
    ) = withContext(Dispatchers.IO) {
        val extractor = MediaExtractorCompat(appContext)
        val muxer = Mp4Muxer.Builder(output.outputStream())
            // Avoid to place metadata reserved space to minimize result file size
            .setAttemptStreamableOutputEnabled(false)
            .build()

        muxer.use { muxer ->
            extractor.setDataSource(input.absolutePath)
            val audioTrackIndex = findFirstAudioTrack(extractor)
            extractor.selectTrack(audioTrackIndex)
            val inputMediaFormat = extractor.getTrackFormat(audioTrackIndex)

            val muxerFormat = MediaFormatUtil.createFormatFromMediaFormat(inputMediaFormat)
            val mp4Timestamp = unixTimeToMp4TimeSeconds(creationTimestamp.toEpochMilli())
                .let { mp4Time -> Mp4TimestampData(mp4Time, mp4Time) }
            val muxerTrackId = muxer.addTrack(muxerFormat)
            muxer.addMetadataEntry(mp4Timestamp)

            do {
                val sampleSize = extractor.sampleSize.toInt()
                val sampleFlags = getBufferFlagsFromMediaCodecFlags(extractor.getSampleFlags())
                val bufferInfo = BufferInfo(extractor.sampleTime, sampleSize, sampleFlags)
                val sampleBuffer = ByteBuffer.allocate(sampleSize)
                extractor.readSampleData(sampleBuffer,  /* offset= */0)
                muxer.writeSampleData(muxerTrackId, sampleBuffer, bufferInfo)
            } while (extractor.advance())

            extractor.release()
        }
    }

    @OptIn(UnstableApi::class)
    private fun findFirstAudioTrack(mediaExtractor: MediaExtractorCompat): Int {
        for (i in 0 until mediaExtractor.trackCount) {
            val format = mediaExtractor.getTrackFormat(i)
            val mimeType = format.getString(MediaFormat.KEY_MIME)
            if (MimeTypes.isAudio(mimeType)) {
                return i
            }
        }
        error("No audio track found")
    }
}
