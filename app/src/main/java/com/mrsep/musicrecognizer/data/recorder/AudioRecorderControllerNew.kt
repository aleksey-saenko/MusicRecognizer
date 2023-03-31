package com.mrsep.musicrecognizer.data.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.mrsep.musicrecognizer.domain.RecordResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AudioRecorderController"

@Singleton
class AudioRecorderControllerNew @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
//    val sampleRateInHz = 48_000
    val sampleRateInHz = 44_100
    val recordFile = File("${appContext.cacheDir.absolutePath}/$RECORD_FILE_NAME")

    private var recorder: AudioRecord? = null

    @SuppressLint("MissingPermission")
//    @WorkerThread
    fun recordToByteArray(duration: Duration): ByteArray {
        Log.d(TAG, "recorder started")
//        val audioSource = MediaRecorder.AudioSource.UNPROCESSED
        val audioSource = MediaRecorder.AudioSource.MIC

        val audioFormat = AudioFormat.Builder()
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRateInHz)
            .build()

        val audioRecord = AudioRecord.Builder()
            .setAudioSource(audioSource)
            .setAudioFormat(audioFormat)
            .build() //throws UnsupportedOperationException

        when (audioRecord.state) { //check for success init
            AudioRecord.STATE_INITIALIZED -> {}
            else -> {}
        }

//        val seconds = catalog.maximumQuerySignatureDurationInMs
        val seconds = duration.seconds.toInt()

        // Final desired buffer size to allocate 12 seconds of audio
        val size = audioFormat.sampleRate * audioFormat.encoding.toByteAllocation() * seconds
        val destination = ByteBuffer.allocate(size)

        // Small buffer to retrieve chunks of audio
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRateInHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // Make sure you are on a dedicated thread or thread pool for mic recording only and
        // elevate the priority to THREAD_PRIORITY_URGENT_AUDIO
//        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)

        audioRecord.startRecording()
        val readBuffer = ByteArray(bufferSize)
        while (destination.remaining() > 0) {
            val actualRead = audioRecord.read(readBuffer, 0, bufferSize) //blocking opp
            val byteArray = readBuffer.sliceArray(0 until actualRead)
            destination.putTrimming(byteArray)
        }
        audioRecord.release()
        Log.d(TAG, "recorder stopped")
        return destination.array()
    }

    private fun Int.toByteAllocation(): Int {
        return when (this) {
            AudioFormat.ENCODING_PCM_16BIT -> 2
            else -> throw IllegalArgumentException("Unsupported encoding")
        }
    }

    private fun ByteBuffer.putTrimming(byteArray: ByteArray) {
        if (byteArray.size <= this.capacity() - this.position()) {
            this.put(byteArray)
        } else {
            this.put(byteArray, 0, this.capacity() - this.position())
        }
    }

    private fun writeToFile(byteArray: ByteArray, filename: String) {
        val outputFile = File("${appContext.filesDir.absolutePath}/$filename")
        outputFile.createNewFile()
        outputFile.outputStream().buffered().use { it.write(byteArray) }
    }

    companion object {
        private const val RECORD_FILE_NAME = "ar_record.raw"
    }


}