package com.h9smartdrive.app.data.vehicle

enum class DataSource(val labelAr: String, val labelEn: String) {
    GWM("نظام GWM", "GWM"),
    ANDROID_AUTOMOTIVE("Android Automotive", "Android Automotive"),
    GPS("نظام GPS", "GPS"),
    OBD("جهاز OBD-II", "OBD-II"),
    DEMO("تجريبي", "Demo"),
    UNAVAILABLE("غير متاح", "Unavailable"),
}

data class Reading<T>(
    val value: T? = null,
    val source: DataSource = DataSource.UNAVAILABLE,
    val updatedAtMillis: Long? = null,
    val staleAfterMillis: Long = 5_000,
) {
    fun isAvailable(): Boolean = value != null
    fun isStale(now: Long = System.currentTimeMillis()): Boolean =
        updatedAtMillis?.let { now - it > staleAfterMillis } ?: true
}

data class TireState(
    val frontLeftPsi: Reading<Double> = Reading(),
    val frontRightPsi: Reading<Double> = Reading(),
    val rearLeftPsi: Reading<Double> = Reading(),
    val rearRightPsi: Reading<Double> = Reading(),
    val frontLeftTempC: Reading<Double> = Reading(),
    val frontRightTempC: Reading<Double> = Reading(),
    val rearLeftTempC: Reading<Double> = Reading(),
    val rearRightTempC: Reading<Double> = Reading(),
)

data class VehicleSnapshot(
    val speedKph: Reading<Double> = Reading(),
    val rpm: Reading<Int> = Reading(),
    val fuelPercent: Reading<Int> = Reading(),
    val rangeKm: Reading<Int> = Reading(),
    val engineTempC: Reading<Int> = Reading(),
    val outsideTempC: Reading<Double> = Reading(),
    val batteryVoltage: Reading<Double> = Reading(),
    val odometerKm: Reading<Double> = Reading(),
    val currentTripKm: Reading<Double> = Reading(),
    val accumulatedTripKm: Reading<Double> = Reading(),
    val averageSpeedKph: Reading<Double> = Reading(),
    val averageFuelConsumptionL100Km: Reading<Double> = Reading(),
    val instantFuelConsumptionL100Km: Reading<Double> = Reading(),
    val transmissionTempC: Reading<Int> = Reading(),
    val steeringAngleDeg: Reading<Double> = Reading(),
    val wheelFrontLeftKph: Reading<Double> = Reading(),
    val wheelFrontRightKph: Reading<Double> = Reading(),
    val wheelRearLeftKph: Reading<Double> = Reading(),
    val wheelRearRightKph: Reading<Double> = Reading(),
    val engineTorqueNm: Reading<Double> = Reading(),
    val gear: Reading<String> = Reading(),
    val driveMode: Reading<String> = Reading(),
    val fourWheelDrive: Reading<String> = Reading(),
    val frontDifferentialLocked: Reading<Boolean> = Reading(),
    val rearDifferentialLocked: Reading<Boolean> = Reading(),
    val doorStatus: Reading<String> = Reading(),
    val altitudeMeters: Reading<Double> = Reading(),
    val headingDegrees: Reading<Float> = Reading(),
    val latitude: Reading<Double> = Reading(),
    val longitude: Reading<Double> = Reading(),
    val tires: TireState = TireState(),
    val providerStatus: Map<DataSource, ProviderStatus> = emptyMap(),
    val demoMode: Boolean = false,
)

data class ProviderStatus(
    val available: Boolean,
    val connected: Boolean,
    val detail: String,
)
