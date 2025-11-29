package com.mrsep.musicrecognizer.core.audio.audiorecord.encoder

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.WavUtil
import androidx.media3.common.util.Util
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "WavWriter"

// Simple WAV file writer derived from Media3's WavFileAudioBufferSink v1.8.0
@UnstableApi
class WavWriter(
    private val outputFile: File,
    private val sampleRate: Int,
    private val channelCount: Int,
    private val pcmEncoding: @C.PcmEncoding Int,
) {

    // scratchBuffer / scratchByteBuffer used for building header values (little-endian)
    private val scratchBuffer = ByteArray(1024)
    private val scratchByteBuffer: ByteBuffer = ByteBuffer
        .wrap(scratchBuffer)
        .order(ByteOrder.LITTLE_ENDIAN)

    private var randomAccessFile: RandomAccessFile? = null
    private var bytesWritten: Int = 0
    private var released = false

    @Throws(IOException::class)
    private fun prepare(): RandomAccessFile {
        check(!released) { "WavWriter is already released" }
        check(randomAccessFile == null) { "WavWriter is already prepared" }

        outputFile.parentFile?.let { parent ->
            if (!parent.exists()) parent.mkdirs()
        }

        val randomAccessFile = RandomAccessFile(outputFile, "rw")
        // Truncate/overwrite existing content
        randomAccessFile.setLength(0)

        // Write the start of the header as big endian data
        randomAccessFile.writeInt(WavUtil.RIFF_FOURCC)
        randomAccessFile.writeInt(-1) // placeholder for file size
        randomAccessFile.writeInt(WavUtil.WAVE_FOURCC)
        randomAccessFile.writeInt(WavUtil.FMT_FOURCC)

        // Write the rest of the header as little endian data
        scratchByteBuffer.clear()
        scratchByteBuffer.putInt(16)
        scratchByteBuffer.putShort(WavUtil.getTypeForPcmEncoding(pcmEncoding).toShort())
        scratchByteBuffer.putShort(channelCount.toShort())
        scratchByteBuffer.putInt(sampleRate)
        val bytesPerSample = Util.getPcmFrameSize(pcmEncoding, channelCount)
        scratchByteBuffer.putInt(bytesPerSample * sampleRate)
        scratchByteBuffer.putShort(bytesPerSample.toShort())
        scratchByteBuffer.putShort((8 * bytesPerSample / channelCount).toShort())
        randomAccessFile.write(scratchBuffer, 0, scratchByteBuffer.position())

        // Write the start of the data chunk as big endian data
        randomAccessFile.writeInt(WavUtil.DATA_FOURCC)
        randomAccessFile.writeInt(-1) // placeholder for data chunk size

        this.randomAccessFile = randomAccessFile
        this.bytesWritten = HEADER_LENGTH
        return randomAccessFile
    }

    @Throws(IOException::class)
    fun write(chunk: ByteArray) {
        check(!released) { "WavWriter has been released; cannot write after release()" }
        if (chunk.isEmpty()) return
        val randomAccessFile = randomAccessFile ?: prepare()
        randomAccessFile.write(chunk, 0, chunk.size)
        bytesWritten += chunk.size
    }

    @Throws(IOException::class)
    fun release() {
        check (!released) { "WavWriter already released" }
        released = true

        val randomAccessFile = randomAccessFile ?: return
        try {
            // Update RIFF chunk size (file size - 8) and data chunk size (bytes of data = total - 44)
            scratchByteBuffer.clear()
            scratchByteBuffer.putInt(bytesWritten - 8)
            randomAccessFile.seek(FILE_SIZE_MINUS_8_OFFSET)
            randomAccessFile.write(scratchBuffer, 0, 4)

            scratchByteBuffer.clear()
            scratchByteBuffer.putInt(bytesWritten - 44)
            randomAccessFile.seek(FILE_SIZE_MINUS_44_OFFSET)
            randomAccessFile.write(scratchBuffer, 0, 4)
        } catch (e: IOException) {
            // The file may still be playable, so just log a warning
            Log.w(TAG, "Error updating file size", e)
        }

        try {
            randomAccessFile.close()
        } finally {
            this.randomAccessFile = null
        }
    }

    companion object {
        private const val FILE_SIZE_MINUS_8_OFFSET = 4L
        private const val FILE_SIZE_MINUS_44_OFFSET = 40L
        private const val HEADER_LENGTH = 44
    }
}
