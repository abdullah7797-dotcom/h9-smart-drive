package com.h9smartdrive.app.data.vehicle

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.math.sin

class DemoDataProvider : VehicleDataProvider {
    override val source = DataSource.DEMO
    private val _status = MutableStateFlow(
        ProviderStatus(available = true, connected = true, detail = "بيانات محاكاة محلية"),
    )
    override val status: Flow<ProviderStatus> = _status

    override val snapshots: Flow<VehicleSnapshot> = flow {
        var tick = 0.0
        while (currentCoroutineContext().isActive) {
            val now = System.currentTimeMillis()
            val speed = 82.0 + sin(tick) * 8.0
            fun <T> demo(value: T, stale: Long = 5_000) =
                Reading(value, DataSource.DEMO, now, stale)

            emit(
                VehicleSnapshot(
                    speedKph = demo(speed),
                    rpm = demo((1850 + sin(tick * 1.3) * 260).toInt()),
                    fuelPercent = demo(68),
                    rangeKm = demo(524),
                    engineTempC = demo(91),
                    outsideTempC = demo(27.0),
                    batteryVoltage = demo(14.2),
                    odometerKm = demo(18_642.4),
                    gear = demo("D6"),
                    driveMode = demo("NORMAL"),
                    fourWheelDrive = demo("4H Auto"),
                    altitudeMeters = demo(612.0),
                    headingDegrees = demo(322f),
                    latitude = demo(24.7136),
                    longitude = demo(46.6753),
                    tires = TireState(
                        frontLeftPsi = demo(34.2),
                        frontRightPsi = demo(34.0),
                        rearLeftPsi = demo(35.1),
                        rearRightPsi = demo(34.8),
                    ),
                    providerStatus = mapOf(DataSource.DEMO to _status.value),
                    demoMode = true,
                ),
            )
            tick += 0.12
            delay(500)
        }
    }

    override suspend fun start() { _status.value = _status.value.copy(connected = true) }
    override suspend fun stop() { _status.value = _status.value.copy(connected = false) }
}
