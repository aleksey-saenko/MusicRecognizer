package com.mrsep.musicrecognizer.core.common

interface Mapper<I, O> {

    fun map(input: I): O
}
