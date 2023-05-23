package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.data.NetworkMonitorData
import com.mrsep.musicrecognizer.feature.recognition.domain.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AdapterNetworkMonitor @Inject constructor(
    private val networkMonitorData: NetworkMonitorData
): NetworkMonitor {

    override val isOffline: Flow<Boolean>
        get() = networkMonitorData.isOffline
    
}