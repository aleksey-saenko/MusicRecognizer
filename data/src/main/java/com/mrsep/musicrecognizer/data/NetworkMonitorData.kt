package com.mrsep.musicrecognizer.data

import kotlinx.coroutines.flow.Flow

/**
 * Utility for reporting app connectivity status inspired by NIA
 */
interface NetworkMonitorData {

    val isOffline: Flow<Boolean>

}