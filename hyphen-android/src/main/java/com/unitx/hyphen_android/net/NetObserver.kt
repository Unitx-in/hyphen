package com.unitx.hyphen_android.net

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import com.unitx.paper.presentation.component.net.NetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NetObserver(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _netStateState = MutableStateFlow(NetState.INIT)
    val netState get() = _netStateState.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _netStateState.update { NetState.OK }
        }

        override fun onLost(network: Network) {
            _netStateState.update { NetState.NOT_OK }
        }

        override fun onUnavailable() {
            _netStateState.update { NetState.NOT_OK }
        }

    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun registerCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun unregisterCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

