package com.mrsep.musicrecognizer.data

import kotlinx.coroutines.flow.Flow

interface NetworkMonitorDo {

    val isOffline: Flow<Boolean>

}