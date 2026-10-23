package com.locup.mvp.platform.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.locup.mvp.domain.model.GeoPoint

/**
 * Reads the last known position using the AOSP [LocationManager].
 *
 * Deliberately avoids Play Services' fused provider: the demo must run on any
 * device (including ones without Google services) and must not require a
 * network round-trip.
 *
 * Every entry point returns null rather than throwing when permission is
 * missing — the app is designed to stay fully usable in that case, using
 * sample coordinates instead.
 */
class AndroidLocationProvider(context: Context) {

    private val appContext = context.applicationContext

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun lastKnownLocation(): GeoPoint? {
        if (!hasPermission()) return null

        val manager = appContext.getSystemService<LocationManager>() ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )

        return providers.mapNotNull { provider ->
            try {
                if (manager.isProviderEnabled(provider)) {
                    manager.getLastKnownLocation(provider)
                } else {
                    null
                }
            } catch (_: SecurityException) {
                // Permission can be revoked between the check and the call.
                null
            } catch (_: IllegalArgumentException) {
                // Provider absent on this device.
                null
            }
        }
            .maxByOrNull { it.time }
            ?.let { GeoPoint(it.latitude, it.longitude) }
    }
}