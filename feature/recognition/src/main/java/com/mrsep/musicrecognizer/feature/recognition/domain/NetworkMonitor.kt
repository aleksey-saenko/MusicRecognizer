package com.mrsep.musicrecognizer.feature.recognition.domain

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {

    val isOffline: Flow<Boolean>
}
