package com.mrsep.musicrecognizer.domain.model

interface Mapper<I, O> {

    fun map(input: I): O

}