package com.h9smartdrive.app.data.vehicle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission") // Permission is polled immediately before registration; SecurityException is also captured.
class GpsDataProvider(private val context: Context) : VehicleDataProvider {
    override val source = DataSource.GPS
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _status = MutableStateFlow(probe())
    override val status: Flow<ProviderStatus> = _status

    override val snapshots: Flow<VehicleSnapshot> = callbackFlow {
        while (!hasPermission()) {
            _status.value = ProviderStatus(
                available = true,
                connected = false,
                detail = "يلزم السماح بالموقع لتفعيل GPS",
            )
            delay(1_000)
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val now = System.currentTimeMillis()
                fun <T> reading(value: T?) = Reading(value, DataSource.GPS, now, 4_000)
                _status.value = ProviderStatus(true, true, "GPS متصل")
                trySend(
                    VehicleSnapshot(
                        speedKph = reading(if (location.hasSpeed()) location.speed * 3.6 else null),
                        altitudeMeters = reading(if (location.hasAltitude()) location.altitude else null),
                        headingDegrees = reading(if (location.hasBearing()) location.bearing else null),
                        latitude = reading(location.latitude),
                        longitude = reading(location.longitude),
                    ),
                )
            }

            override fun onProviderEnabled(provider: String) {
                _status.value = ProviderStatus(true, false, "GPS ينتظر أول قراءة")
            }

            override fun onProviderDisabled(provider: String) {
                _status.value = ProviderStatus(true, false, "GPS متوقف من إعدادات النظام")
            }

            @Deprecated("Deprecated in Android")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        runCatching {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500L,
                0f,
                listener,
                Looper.getMainLooper(),
            )
            _status.value = ProviderStatus(true, false, "GPS ينتظر أول قراءة")
        }.onFailure {
            _status.value = ProviderStatus(false, false, "تعذر تشغيل GPS: ${it.javaClass.simpleName}")
            close(it)
        }

        awaitClose { locationManager.removeUpdates(listener) }
    }

    private fun hasPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun probe(): ProviderStatus {
        val available = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        return ProviderStatus(
            available = available,
            connected = false,
            detail = if (available) "GPS متاح" else "لا يوجد مستقبل GPS معلن",
        )
    }

    override suspend fun start() { _status.value = probe() }
    override suspend fun stop() { _status.value = _status.value.copy(connected = false) }
}
