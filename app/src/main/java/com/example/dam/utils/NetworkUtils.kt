package com.example.dam.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * ✅ Utilitaire pour vérifier la connectivité réseau
 * Utilisé pour le mode hors ligne dès le démarrage
 */
object NetworkUtils {
    private const val TAG = "NetworkUtils"

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable

    /**
     * Vérifier si le réseau est disponible
     * @param context Context Android
     * @return true si connecté, false sinon
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            Log.w(TAG, "⚠️ ConnectivityManager not available")
            return false
        }

        return try {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            val isConnected = capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )

            Log.d(TAG, "🌐 Network available: $isConnected")
            _isNetworkAvailable.value = isConnected
            isConnected
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking network: ${e.message}")
            false
        }
    }

    /**
     * Vérifier si le réseau est disponible avec tentative de ping
     * Plus fiable pour détecter une vraie connexion internet
     */
    fun isInternetReachable(context: Context): Boolean {
        if (!isNetworkAvailable(context)) {
            return false
        }

        return try {
            // Tentative de ping rapide vers Google DNS
            val runtime = Runtime.getRuntime()
            val process = runtime.exec("/system/bin/ping -c 1 -W 1 8.8.8.8")
            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Internet reachability check failed: ${e.message}")
            // En cas d'erreur, on se fie juste à isNetworkAvailable
            isNetworkAvailable(context)
        }
    }

    /**
     * Observer les changements de connectivité réseau
     */
    fun observeNetworkStatus(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "🟢 Network available")
                _isNetworkAvailable.value = true
                trySend(true)
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "🔴 Network lost")
                _isNetworkAvailable.value = false
                trySend(false)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val isConnected = hasInternet && hasValidated

                Log.d(TAG, "📶 Network capabilities changed: connected=$isConnected")
                _isNetworkAvailable.value = isConnected
                trySend(isConnected)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Envoyer l'état initial
        trySend(isNetworkAvailable(context))

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}

