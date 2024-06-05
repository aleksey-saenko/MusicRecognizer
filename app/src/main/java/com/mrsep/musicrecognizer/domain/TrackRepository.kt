package com.mrsep.musicrecognizer.domain

import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getUnviewedCountFlow(): Flow<Int>
}
