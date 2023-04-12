package com.mrsep.musicrecognizer.data.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AudioRecorderControllerFlow"


//https://github.com/dburckh/EncodeRawAudio
//https://github.com/OnlyInAmerica/HWEncoderExperiments
//https://habr.com/ru/post/233005/
//https://developer.android.com/reference/android/media/MediaCodec#data-processing
//https://developer.android.com/reference/android/media/MediaMuxer
//https://developer.android.com/reference/java/nio/ByteBuffer


@Singleton
class AudioRecorderControllerFlow @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    //    val sampleRateInHz = 48_000
    val sampleRateInHz = 44_100
    val recordFile = File("${appContext.cacheDir.absolutePath}/$RECORD_FILE_NAME")

    private var recorder: AudioRecord? = null

    val recordFlow: Flow<FloatArray> = flow {
        Log.d(TAG, "recorder started")
        val audioSource = MediaRecorder.AudioSource.MIC

        val audioFormat = AudioFormat.Builder()
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setSampleRate(sampleRateInHz)
            .build()

        @SuppressLint("MissingPermission")
        val audioRecord = AudioRecord.Builder()
            .setAudioSource(audioSource)
            .setAudioFormat(audioFormat)
            .build()

        // Small buffer to retrieve chunks of audio
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRateInHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        println("internal bufferSize: $bufferSize")

        val seconds = 12

        // Final desired buffer size to allocate 12 seconds of audio
//        val size = audioFormat.sampleRate * audioFormat.encoding.toByteAllocation() * chunkSize
//        val size3 = audioFormat.sampleRate * audioFormat.encoding.toByteAllocation() * 3
//        val destination = ByteBuffer.allocate(size)

        // Make sure you are on a dedicated thread or thread pool for mic recording only and
        // elevate the priority to THREAD_PRIORITY_URGENT_AUDIO
//        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        audioRecord.startRecording()
//        val readBuffer = FloatArray(bufferSize)

        try {
            while (true) {
//                val destination = ByteBuffer.allocate(size)
//                while (destination.remaining() > 0) {
                val readBuffer = FloatArray(bufferSize)
                val actualRead = audioRecord.read(
                    readBuffer,
                    0,
                    bufferSize,
                    AudioRecord.READ_NON_BLOCKING
                ) //blocking opp
                println("actualRead=$actualRead")
//                emit(readBuffer)
                emit(readBuffer.copyOf(actualRead))
//                    val byteArray = readBuffer.sliceArray(0 until actualRead)
//                    destination.putTrimming(byteArray)
//                }
//                emit(destination)
//                trySend(destination.duplicate())
//                destination.clear()
            }
        } finally {
            audioRecord.release()
            Log.d(TAG, "recorder stopped")
        }
    }
        .flowOn(Dispatchers.Default)

    private val pitStops = listOf(
        (3 * 44100 - 7104 / 2..3 * 44100 + 7104 / 2),
        (6 * 44100 - 7104 / 2..6 * 44100 + 7104 / 2),
        (9 * 44100 - 7104 / 2..9 * 44100 + 7104 / 2)
    )

    fun test() {
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            recordFlow.filter { it.isNotEmpty() && it.size < 1000 }.collect {
                main(ByteBuffer.wrap(floatArrayToByteArray(it)))
            }
        }
    }

    fun calculatedFlow(maxLengthInSec: Int): Flow<ByteArray> = recordFlow
        .runningFold(FloatBuffer.allocate(maxLengthInSec * 44100)) { buffer: FloatBuffer, value ->
            buffer.put(value)
        }
        .transform { buffer ->
            if (pitStops.any { pitStop -> buffer.position() in pitStop }) {
                emit(Arrays.copyOf(buffer.array(), buffer.position()))
            }
        }
        .take(3)
        .map {
            val array = floatArrayToByteArray(it)
//            main(ByteBuffer.wrap(array))
            array
        }
        .flowOn(Dispatchers.Default)

    private fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(floatArray.size * 4)
        for (i in floatArray.indices) {
            byteBuffer.putFloat(floatArray[i])
        }
        return byteBuffer.array()
    }

    fun main(byteBufferParam: ByteBuffer) {
        println("byteBufferParam: ${byteBufferParam!!.capacity()}, remaining=${byteBufferParam.remaining()}, position=${byteBufferParam.position()}")
        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        val channelMask = AudioFormat.CHANNEL_IN_MONO
        val channels = Integer.bitCount(channelMask)
        val sampleRate = 44_100
        val mediaFormat =
            MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channels)
        val ENCODER_BIT_RATE = 160 * 1024
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, ENCODER_BIT_RATE)
        mediaFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        val outputFormat = codec.outputFormat // option B
        codec.start()

        val inputBufferId = codec.dequeueInputBuffer(0)
        if (inputBufferId >= 0) {
            val inputBuffer = codec.getInputBuffer(inputBufferId)
            println("inputBuffer: ${inputBuffer!!.capacity()}, remaining=${inputBuffer.remaining()}, position=${inputBuffer.position()}")
            inputBuffer!!.put(byteBufferParam) // fill inputBuffer with valid data
            codec.queueInputBuffer(inputBufferId, 0, byteBufferParam.capacity(), 0, 0)
        }
        val outputBufferId = codec.dequeueOutputBuffer(MediaCodec.BufferInfo(), 1_000_000)
        println("outputBufferId=$outputBufferId")
        if (outputBufferId >= 0) {
            val outputBuffer = codec.getOutputBuffer(outputBufferId)
            val bufferFormat = codec.getOutputFormat(outputBufferId)
            // bufferFormat is identical to outputFormat
            // outputBuffer is ready to be processed or rendered.
            codec.releaseOutputBuffer(outputBufferId, false)
            println("outputBuffer: capacity=${outputBuffer!!.capacity()}")
        }
        codec.stop()
        codec.release()
    }


//    fun getRecordFlow(chunkSize: Int) = flow<ByteBuffer> {
//        Log.d(TAG, "recorder started")
//        val audioSource = MediaRecorder.AudioSource.MIC
//
//        val audioFormat = AudioFormat.Builder()
//            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
//            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//            .setSampleRate(sampleRateInHz)
//            .build()
//
//        @SuppressLint("MissingPermission")
//        val audioRecord = AudioRecord.Builder()
//            .setAudioSource(audioSource)
//            .setAudioFormat(audioFormat)
//            .build()
//
//        // Small buffer to retrieve chunks of audio
//        val bufferSize = AudioRecord.getMinBufferSize(
//            sampleRateInHz,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT
//        )
//        println("internal bufferSize: $bufferSize")
//
//        val seconds = 12
//
//        // Final desired buffer size to allocate 12 seconds of audio
//        val size = audioFormat.sampleRate * audioFormat.encoding.toByteAllocation() * chunkSize
////        val size3 = audioFormat.sampleRate * audioFormat.encoding.toByteAllocation() * 3
////        val destination = ByteBuffer.allocate(size)
//
//        // Make sure you are on a dedicated thread or thread pool for mic recording only and
//        // elevate the priority to THREAD_PRIORITY_URGENT_AUDIO
////        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
//        audioRecord.startRecording()
//        val readBuffer = ByteArray(bufferSize)
//
//        try {
//            while (true) {
//                val destination = ByteBuffer.allocate(size)
//                while (destination.remaining() > 0) {
//                    val actualRead = audioRecord.read(readBuffer, 0, bufferSize, AudioRecord.READ_BLOCKING) //blocking opp
//                    println("actualRead=$actualRead")
//                    val byteArray = readBuffer.sliceArray(0 until actualRead)
//                    destination.putTrimming(byteArray)
//                }
//                emit(destination)
////                trySend(destination.duplicate())
////                destination.clear()
//            }
//        } finally {
//            audioRecord.release()
//            Log.d(TAG, "recorder stopped")
//        }
//    }
//        .flowOn(Dispatchers.Default)

//    @SuppressLint("MissingPermission")
////    @WorkerThread
//    fun recordToByteArray(duration: Duration): ByteArray {
//        Log.d(TAG, "recorder started")
////        val audioSource = MediaRecorder.AudioSource.UNPROCESSED
//        val audioSource = MediaRecorder.AudioSource.MIC
//
//        val audioFormat = AudioFormat.Builder()
//            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
//            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//            .setSampleRate(sampleRateInHz)
//            .build()
//
//        val audioRecord = AudioRecord.Builder()
//            .setAudioSource(audioSource)
//            .setAudioFormat(audioFormat)
//            .build() //throws UnsupportedOperationException
//
//        when (audioRecord.state) { //check for success init
//            AudioRecord.STATE_INITIALIZED -> {}
//            else -> {}
//        }
//
////        val seconds = catalog.maximumQuerySignatureDurationInMs
//        val seconds = duration.seconds.toInt()
//
//        // Final desired buffer size to allocate 12 seconds of audio
//        val size = audioFormat.sampleRate * audioFormat.encoding.toByteAllocation() * seconds
//        val destination = ByteBuffer.allocate(size)
//
//        // Small buffer to retrieve chunks of audio
//        val bufferSize = AudioRecord.getMinBufferSize(
//            sampleRateInHz,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT
//        )
////        Log.d("2703", "min = $bufferSize")
////        println("MINIMUM: $bufferSize")
//
//        // Make sure you are on a dedicated thread or thread pool for mic recording only and
//        // elevate the priority to THREAD_PRIORITY_URGENT_AUDIO
////        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
//        audioRecord.startRecording()
//        val readBuffer = ByteArray(bufferSize)
//        while (destination.remaining() > 0) {
//            val actualRead = audioRecord.read(readBuffer, 0, bufferSize) //blocking opp
//            val byteArray = readBuffer.sliceArray(0 until actualRead)
//            destination.putTrimming(byteArray)
//        }
//        audioRecord.release()
//        Log.d(TAG, "recorder stopped")
//        return destination.array()
//    }

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