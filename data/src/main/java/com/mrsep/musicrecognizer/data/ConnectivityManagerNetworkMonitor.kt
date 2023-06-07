package com.mrsep.musicrecognizer.data

import android.content.Context
import android.net.ConnectivityManager

import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class ConnectivityManagerNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkMonitorDo {
    override val isOffline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        if (connectivityManager == null) {
            channel.trySend(false)
            channel.close()
            return@callbackFlow
        }


        fun update() {
            channel.trySend(connectivityManager.isCurrentlyConnected().not())
        }

        /**
         * The callback's methods are invoked on changes to *any* network, not just the active
         * network. So to check for network connectivity, one must query the active network of the
         * ConnectivityManager.
         */
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = update()

            override fun onLost(network: Network) = update()

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) = update()
        }

        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback,
        )

        update()

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        .conflate()


    private fun ConnectivityManager.isCurrentlyConnected() =
        activeNetwork
            ?.let(::getNetworkCapabilities)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

}