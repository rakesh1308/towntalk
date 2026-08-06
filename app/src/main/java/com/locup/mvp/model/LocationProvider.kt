package com.locup.mvp.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

/**
 * Lightweight location provider built on the AOSP LocationManager (no
 * Google Play Services dependency). Returns the last known fix or null.
 */
class LocationProvider(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun lastKnown(): Location? {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return null

        val lm: LocationManager = context.getSystemService() ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        return providers.mapNotNull { p ->
            try { if (lm.isProviderEnabled(p)) lm.getLastKnownLocation(p) else null } catch (_: Throwable) { null }
        }.maxByOrNull { it.time }
    }
}
