package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.toShortArray(): ShortArray {
    val output = ShortArray(size / Short.SIZE_BYTES)
    ByteBuffer.wrap(this).order(ByteOrder.nativeOrder()).asShortBuffer().get(output)
    return output
}

fun ByteArray.toFloatArray(): FloatArray {
    val output = FloatArray(size / Float.SIZE_BYTES)
    ByteBuffer.wrap(this).order(ByteOrder.nativeOrder()).asFloatBuffer().get(output)
    return output
}

fun FloatArray.toByteArray(): ByteArray {
    return ByteBuffer.allocate(this.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .apply { asFloatBuffer().put(this@toByteArray) }
        .array()
}