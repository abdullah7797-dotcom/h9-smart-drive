package com.h9smartdrive.app.data.vehicle

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleDataManager(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val demo = DemoDataProvider()
    private val providers = listOf(
        GwmVehicleDataProvider(context),
        AndroidAutomotiveDataProvider(context),
        GpsDataProvider(context),
        ObdDataProvider(),
    )
    private val _snapshot = MutableStateFlow(VehicleSnapshot())
    val snapshot: StateFlow<VehicleSnapshot> = _snapshot.asStateFlow()
    private var demoJob: Job? = null
    private var demoEnabled = false

    init {
        val statuses = mutableMapOf<DataSource, ProviderStatus>()
        providers.forEach { provider ->
            scope.launch {
                provider.start()
                provider.status.collect { status ->
                    statuses[provider.source] = status
                    _snapshot.value = _snapshot.value.copy(providerStatus = statuses.toMap())
                }
            }
            scope.launch {
                provider.snapshots.collect { incoming ->
                    if (!demoEnabled) _snapshot.value = merge(_snapshot.value, incoming)
                }
            }
        }
    }

    fun setDemoMode(enabled: Boolean) {
        if (demoEnabled == enabled) return
        demoEnabled = enabled
        demoJob?.cancel()
        demoJob = null
        if (enabled) {
            demoJob = scope.launch {
                demo.snapshots.collect { incoming ->
                    _snapshot.value = incoming.copy(
                        providerStatus = _snapshot.value.providerStatus + incoming.providerStatus,
                    )
                }
            }
        } else {
            _snapshot.value = VehicleSnapshot(providerStatus = _snapshot.value.providerStatus)
        }
    }

    private fun merge(current: VehicleSnapshot, incoming: VehicleSnapshot): VehicleSnapshot = current.copy(
        speedKph = best(current.speedKph, incoming.speedKph),
        rpm = best(current.rpm, incoming.rpm),
        fuelPercent = best(current.fuelPercent, incoming.fuelPercent),
        rangeKm = best(current.rangeKm, incoming.rangeKm),
        engineTempC = best(current.engineTempC, incoming.engineTempC),
        outsideTempC = best(current.outsideTempC, incoming.outsideTempC),
        batteryVoltage = best(current.batteryVoltage, incoming.batteryVoltage),
        odometerKm = best(current.odometerKm, incoming.odometerKm),
        currentTripKm = best(current.currentTripKm, incoming.currentTripKm),
        accumulatedTripKm = best(current.accumulatedTripKm, incoming.accumulatedTripKm),
        averageSpeedKph = best(current.averageSpeedKph, incoming.averageSpeedKph),
        averageFuelConsumptionL100Km = best(current.averageFuelConsumptionL100Km, incoming.averageFuelConsumptionL100Km),
        instantFuelConsumptionL100Km = best(current.instantFuelConsumptionL100Km, incoming.instantFuelConsumptionL100Km),
        transmissionTempC = best(current.transmissionTempC, incoming.transmissionTempC),
        steeringAngleDeg = best(current.steeringAngleDeg, incoming.steeringAngleDeg),
        wheelFrontLeftKph = best(current.wheelFrontLeftKph, incoming.wheelFrontLeftKph),
        wheelFrontRightKph = best(current.wheelFrontRightKph, incoming.wheelFrontRightKph),
        wheelRearLeftKph = best(current.wheelRearLeftKph, incoming.wheelRearLeftKph),
        wheelRearRightKph = best(current.wheelRearRightKph, incoming.wheelRearRightKph),
        engineTorqueNm = best(current.engineTorqueNm, incoming.engineTorqueNm),
        gear = best(current.gear, incoming.gear),
        driveMode = best(current.driveMode, incoming.driveMode),
        fourWheelDrive = best(current.fourWheelDrive, incoming.fourWheelDrive),
        frontDifferentialLocked = best(current.frontDifferentialLocked, incoming.frontDifferentialLocked),
        rearDifferentialLocked = best(current.rearDifferentialLocked, incoming.rearDifferentialLocked),
        doorStatus = best(current.doorStatus, incoming.doorStatus),
        altitudeMeters = best(current.altitudeMeters, incoming.altitudeMeters),
        headingDegrees = best(current.headingDegrees, incoming.headingDegrees),
        latitude = best(current.latitude, incoming.latitude),
        longitude = best(current.longitude, incoming.longitude),
        tires = TireState(
            frontLeftPsi = best(current.tires.frontLeftPsi, incoming.tires.frontLeftPsi),
            frontRightPsi = best(current.tires.frontRightPsi, incoming.tires.frontRightPsi),
            rearLeftPsi = best(current.tires.rearLeftPsi, incoming.tires.rearLeftPsi),
            rearRightPsi = best(current.tires.rearRightPsi, incoming.tires.rearRightPsi),
            frontLeftTempC = best(current.tires.frontLeftTempC, incoming.tires.frontLeftTempC),
            frontRightTempC = best(current.tires.frontRightTempC, incoming.tires.frontRightTempC),
            rearLeftTempC = best(current.tires.rearLeftTempC, incoming.tires.rearLeftTempC),
            rearRightTempC = best(current.tires.rearRightTempC, incoming.tires.rearRightTempC),
        ),
    )

    private fun <T> best(current: Reading<T>, incoming: Reading<T>): Reading<T> {
        if (!incoming.isAvailable()) return if (current.isStale()) Reading() else current
        if (!current.isAvailable() || current.isStale()) return incoming
        return if (incoming.source.ordinal <= current.source.ordinal) incoming else current
    }
}
