package com.mrsep.musicrecognizer.core.common

interface BidirectionalMapper<I, O> : Mapper<I, O> {

    fun reverseMap(input: O): I

}