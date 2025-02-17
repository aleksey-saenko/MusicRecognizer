package com.mrsep.musicrecognizer.core.domain.recognition

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {

    val isOffline: Flow<Boolean>
}
