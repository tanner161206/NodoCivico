package com.nodocivico.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Método moderno (API 23+)
        val network = cm.activeNetwork ?: return true // si no detecta, asume conectado
        val caps = cm.getNetworkCapabilities(network) ?: return true

        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}