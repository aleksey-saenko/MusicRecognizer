package com.mrsep.musicrecognizer.data.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import java.nio.ByteBuffer
import android.os.Process
import android.Manifest
import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/*
* android.media.AudioRecord
* https://habr.com/ru/post/137708/
* https://developer.android.com/reference/android/media/AudioRecord
* https://developer.apple.com/shazamkit/android/
*/
@Singleton
class AudioRecorderController @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @WorkerThread
     fun simpleMicRecording() : ByteArray {
        Toast.makeText(applicationContext, "recorder started", Toast.LENGTH_LONG).show()
        val audioSource = MediaRecorder.AudioSource.UNPROCESSED

        val audioFormat = AudioFormat.Builder()
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(48_000)
            .build()

        val audioRecord = AudioRecord.Builder()
            .setAudioSource(audioSource)
            .setAudioFormat(audioFormat)
            .build()

//        val seconds = catalog.maximumQuerySignatureDurationInMs
        val seconds = 12

        // Final desired buffer size to allocate 12 seconds of audio
        val size = audioFormat.sampleRate * audioFormat.encoding.toByteAllocation() * seconds
        val destination = ByteBuffer.allocate(size)

        // Small buffer to retrieve chunks of audio
        val bufferSize = AudioRecord.getMinBufferSize(
            48_000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // Make sure you are on a dedicated thread or thread pool for mic recording only and
        // elevate the priority to THREAD_PRIORITY_URGENT_AUDIO
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)

        audioRecord.startRecording()
        val readBuffer = ByteArray(bufferSize)
        while (destination.remaining()>0) {
            val actualRead = audioRecord.read(readBuffer, 0, bufferSize)
            val byteArray = readBuffer.sliceArray(0 until actualRead)
            destination.putTrimming(byteArray)
        }
        audioRecord.release()
        writeToCacheDir(destination.array())
        Toast.makeText(applicationContext, "recorder stopped", Toast.LENGTH_LONG).show()
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

    private fun writeToCacheDir(byteArray: ByteArray) {
        getRecordFile(applicationContext).outputStream().buffered().use { it.write(byteArray) }
    }

    companion object {
        private const val recordFileName = "ar_record.raw"
        fun getRecordFile(context: Context) = File("${context.cacheDir.absolutePath}/$recordFileName")
    }


}

