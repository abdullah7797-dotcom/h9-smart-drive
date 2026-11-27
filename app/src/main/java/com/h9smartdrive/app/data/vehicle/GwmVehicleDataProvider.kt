package com.h9smartdrive.app.data.vehicle

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Read-only compatibility path for the SA8155 H9 head unit and its factory GWM adapter.
 *
 * Only GET_DATA and data-change listener transactions are used. No setter transaction, CAN
 * command, diagnostic request, vehicle identity, or configuration value is requested.
 */
class GwmVehicleDataProvider(private val context: Context) : VehicleDataProvider, ServiceConnection {
    override val source = DataSource.GWM
    private val _status = MutableStateFlow(probe())
    override val status: Flow<ProviderStatus> = _status
    private val _snapshots = MutableStateFlow(VehicleSnapshot())
    override val snapshots: Flow<VehicleSnapshot> = _snapshots

    private val providerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val rawValues = arrayOfNulls<String>(DATA_IDS.size)
    private val dataListener = DataChangedBinder(::onDataChanged)
    private var service: IBinder? = null
    private var bound = false
    private var listenerRegistered = false
    private var pollingJob: Job? = null

    override suspend fun start() {
        val initial = probe()
        _status.value = initial
        if (!initial.available || bound) return
        withContext(Dispatchers.Main.immediate) {
            val intent = Intent().setClassName(SERVICE_PACKAGE, SERVICE_CLASS)
            runCatching { context.bindService(intent, this@GwmVehicleDataProvider, Context.BIND_AUTO_CREATE) }
                .onSuccess { accepted ->
                    bound = accepted
                    if (!accepted) _status.value = ProviderStatus(true, false, "خدمة GWM موجودة لكنها رفضت الربط")
                }
                .onFailure { error ->
                    _status.value = ProviderStatus(true, false, "تعذر ربط GWM: ${error.javaClass.simpleName}")
                }
        }
    }

    override suspend fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        unregisterListener()
        service = null
        if (bound) {
            withContext(Dispatchers.Main.immediate) {
                runCatching { context.unbindService(this@GwmVehicleDataProvider) }
            }
        }
        bound = false
        _status.value = _status.value.copy(connected = false, detail = "تم إيقاف موفّر GWM")
        providerScope.cancel()
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = binder
        listenerRegistered = registerListener(binder)
        _status.value = ProviderStatus(
            true,
            false,
            if (listenerRegistered) "تم ربط GWM ومستمع البيانات اللحظية" else "تم ربط GWM؛ استخدام القراءة الدورية",
        )
        pollingJob?.cancel()
        pollingJob = providerScope.launch {
            while (isActive) {
                readAll(binder)?.let(::publish)
                delay(if (listenerRegistered) LISTENER_SAFETY_POLL_MS else POLL_INTERVAL_MS)
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        pollingJob?.cancel()
        pollingJob = null
        service = null
        listenerRegistered = false
        _status.value = ProviderStatus(true, false, "انقطع اتصال خدمة GWM؛ سيحاول Android إعادة الربط")
    }

    override fun onBindingDied(name: ComponentName) = onServiceDisconnected(name)

    override fun onNullBinding(name: ComponentName) {
        onServiceDisconnected(name)
        _status.value = ProviderStatus(true, false, "خدمة GWM أعادت Binder فارغًا")
    }

    private fun probe(): ProviderStatus {
        val installed = runCatching { context.packageManager.getPackageInfo(SERVICE_PACKAGE, 0) }.isSuccess
        return ProviderStatus(
            available = installed,
            connected = false,
            detail = if (installed) "ملف H9 SA8155 · محوّل GWM v9" else "محوّل GWM غير موجود؛ سيتم تجربة Android Automotive",
        )
    }

    private fun onDataChanged(id: String?, value: String?) {
        val index = DATA_IDS.indexOf(id)
        if (index < 0) return
        rawValues[index] = value.normalized()
        providerScope.launch { publish(rawValues.copyOf()) }
    }

    private fun readAll(binder: IBinder): Array<String?>? {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(SERVICE_DESCRIPTOR)
            data.writeInt(1)
            data.writeString(context.packageName)
            data.writeStringArray(DATA_IDS)
            data.writeStringArray(null)
            data.writeInt(1)
            if (!binder.transact(TRANSACTION_GET_DATA, data, reply, 0)) return null
            reply.readException()
            reply.createStringArray()?.map { it.normalized() }?.toTypedArray()
        } catch (error: Throwable) {
            _status.value = ProviderStatus(true, false, "رفض قراءة GWM: ${error.rootCauseName()}")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun publish(values: Array<String?>) {
        val count = minOf(values.size, rawValues.size)
        for (index in 0 until count) rawValues[index] = values[index]
        val snapshot = rawValues.toVehicleSnapshot()
        _snapshots.value = snapshot
        val liveCount = snapshot.availableReadingCount()
        val rawCount = rawValues.count { it != null }
        _status.value = if (liveCount > 0) {
            ProviderStatus(
                true,
                true,
                "H9 SA8155 · $liveCount قيمة معروضة · $rawCount إشارة خام" +
                    if (listenerRegistered) " · تحديث لحظي" else " · تحديث دوري",
            )
        } else {
            ProviderStatus(true, false, "الخدمة متصلة لكن لم تُرجع أرقامًا صالحة")
        }
    }

    private fun registerListener(binder: IBinder): Boolean {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(SERVICE_DESCRIPTOR)
            data.writeString(context.packageName)
            data.writeStringArray(DATA_IDS)
            data.writeStrongBinder(dataListener)
            binder.transact(TRANSACTION_REGISTER_LISTENER, data, reply, 0) && run {
                reply.readException()
                reply.readInt() != 0
            }
        } catch (_: Throwable) {
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun unregisterListener() {
        val binder = service ?: return
        if (!listenerRegistered) return
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(SERVICE_DESCRIPTOR)
            data.writeString(context.packageName)
            data.writeStrongBinder(dataListener)
            if (binder.transact(TRANSACTION_UNREGISTER_LISTENER, data, reply, 0)) {
                reply.readException()
                reply.readInt()
            }
        } catch (_: Throwable) {
            // The service can disappear while Android is tearing down the connection.
        } finally {
            data.recycle()
            reply.recycle()
            listenerRegistered = false
        }
    }

    private fun Array<String?>.toVehicleSnapshot(): VehicleSnapshot {
        val now = System.currentTimeMillis()
        fun number(index: Int, min: Double, max: Double): Double? =
            getOrNull(index)?.toDoubleOrNull()?.takeIf { it.isFinite() && it in min..max }
        fun text(index: Int): String? = getOrNull(index).normalized()
        fun flag(index: Int): Boolean? = text(index)?.let { raw ->
            when (raw.lowercase()) {
                "true", "on", "active" -> true
                "false", "off", "inactive" -> false
                else -> raw.toDoubleOrNull()?.let { it != 0.0 }
            }
        }
        fun <T> reading(value: T?) = Reading(value, DataSource.GWM, now, STALE_AFTER_MS)

        val selector = when (number(INDEX_GEAR_STATUS, 0.0, 10.0)?.roundToInt()) {
            0 -> "N"
            2 -> "D"
            3 -> "P"
            4 -> "R"
            5 -> "M"
            else -> null
        }
        val ratio = number(INDEX_CURRENT_GEAR, 0.0, 9.0)?.roundToInt()?.let { raw ->
            when {
                raw in 1..7 -> raw
                raw == 9 -> 8
                else -> null
            }
        }
        val gear = when {
            selector == "D" || selector == "M" -> selector + (ratio?.toString() ?: "")
            selector != null -> selector
            ratio != null -> ratio.toString()
            else -> null
        }

        val driveModeRaw = text(INDEX_DRIVE_MODE)
            ?: text(INDEX_EXPERT_DRIVE_MODE)
            ?: text(INDEX_OFFROAD_DRIVE_MODE)
        val frontDiff = flag(INDEX_FRONT_DIFFERENTIAL_LOCK)
        val rearDiff = flag(INDEX_REAR_DIFFERENTIAL_LOCK)
        val fourWheelRaw = text(INDEX_EXPERT_FOUR_WHEEL_DRIVE)
        val fourWheelDrive = when {
            frontDiff == true && rearDiff == true -> "4WD · FRONT+REAR LOCK"
            rearDiff == true -> "4WD · REAR LOCK"
            frontDiff == true -> "4WD · FRONT LOCK"
            driveModeRaw == "11" -> "AWD"
            fourWheelRaw != null -> "4WD $fourWheelRaw"
            else -> null
        }
        val tpms = GwmValueDecoder.parseTpms(text(INDEX_TPMS))

        return VehicleSnapshot(
            speedKph = reading(number(INDEX_SPEED, 0.0, 260.0)),
            rpm = reading(number(INDEX_RPM, 0.0, 9_000.0)?.roundToInt()),
            odometerKm = reading(number(INDEX_ODOMETER, 0.0, 2_000_000.0)),
            currentTripKm = reading(number(INDEX_CURRENT_TRIP, 0.0, 100_000.0)),
            accumulatedTripKm = reading(number(INDEX_ACCUMULATED_TRIP, 0.0, 2_000_000.0)),
            averageSpeedKph = reading(
                number(INDEX_AVERAGE_SPEED_SINCE_STARTUP, 0.0, 260.0)
                    ?: number(INDEX_AVERAGE_SPEED_SINCE_RESET, 0.0, 260.0),
            ),
            fuelPercent = reading(number(INDEX_FUEL_PERCENT, 0.0, 100.0)?.roundToInt()),
            rangeKm = reading(number(INDEX_RANGE, 0.0, 3_000.0)?.roundToInt()),
            averageFuelConsumptionL100Km = reading(
                number(INDEX_CURRENT_AVERAGE_CONSUMPTION_A, 0.0, 100.0)
                    ?: number(INDEX_CURRENT_AVERAGE_CONSUMPTION_ALIAS, 0.0, 100.0)
                    ?: number(INDEX_AVERAGE_CONSUMPTION, 0.0, 100.0),
            ),
            instantFuelConsumptionL100Km = reading(number(INDEX_INSTANT_CONSUMPTION, 0.0, 200.0)),
            engineTempC = reading(number(INDEX_COOLANT, -40.0, 180.0)?.roundToInt()),
            transmissionTempC = reading(number(INDEX_TRANSMISSION_TEMP, -40.0, 220.0)?.roundToInt()),
            outsideTempC = reading(number(INDEX_OUTSIDE_TEMP, -60.0, 80.0)),
            gear = reading(gear),
            batteryVoltage = reading(number(INDEX_BATTERY_VOLTAGE, 5.0, 20.0)),
            driveMode = reading(driveModeRaw?.let(GwmValueDecoder::driveModeLabel)),
            fourWheelDrive = reading(fourWheelDrive),
            frontDifferentialLocked = reading(frontDiff),
            rearDifferentialLocked = reading(rearDiff),
            doorStatus = reading(text(INDEX_DOOR_STATUS)),
            steeringAngleDeg = reading(number(INDEX_STEERING_ANGLE, -1_080.0, 1_080.0)),
            wheelFrontLeftKph = reading(number(INDEX_WHEEL_FRONT_LEFT, 0.0, 350.0)),
            wheelFrontRightKph = reading(number(INDEX_WHEEL_FRONT_RIGHT, 0.0, 350.0)),
            wheelRearLeftKph = reading(number(INDEX_WHEEL_REAR_LEFT, 0.0, 350.0)),
            wheelRearRightKph = reading(number(INDEX_WHEEL_REAR_RIGHT, 0.0, 350.0)),
            engineTorqueNm = reading(number(INDEX_ENGINE_TORQUE, -2_000.0, 2_000.0)),
            tires = TireState(
                frontLeftPsi = reading(tpms.pressurePsi.getOrNull(0)),
                frontRightPsi = reading(tpms.pressurePsi.getOrNull(1)),
                rearLeftPsi = reading(tpms.pressurePsi.getOrNull(2)),
                rearRightPsi = reading(tpms.pressurePsi.getOrNull(3)),
                frontLeftTempC = reading(tpms.temperatureC.getOrNull(0)),
                frontRightTempC = reading(tpms.temperatureC.getOrNull(1)),
                rearLeftTempC = reading(tpms.temperatureC.getOrNull(2)),
                rearRightTempC = reading(tpms.temperatureC.getOrNull(3)),
            ),
        )
    }

    private fun VehicleSnapshot.availableReadingCount(): Int = listOf(
        speedKph.value, rpm.value, odometerKm.value, currentTripKm.value, accumulatedTripKm.value,
        averageSpeedKph.value, fuelPercent.value, rangeKm.value, averageFuelConsumptionL100Km.value,
        instantFuelConsumptionL100Km.value, engineTempC.value, transmissionTempC.value,
        outsideTempC.value, gear.value, batteryVoltage.value, driveMode.value, fourWheelDrive.value,
        frontDifferentialLocked.value, rearDifferentialLocked.value, doorStatus.value,
        steeringAngleDeg.value, wheelFrontLeftKph.value, wheelFrontRightKph.value,
        wheelRearLeftKph.value, wheelRearRightKph.value, engineTorqueNm.value,
        tires.frontLeftPsi.value, tires.frontRightPsi.value, tires.rearLeftPsi.value,
        tires.rearRightPsi.value, tires.frontLeftTempC.value, tires.frontRightTempC.value,
        tires.rearLeftTempC.value, tires.rearRightTempC.value,
    ).count { it != null }

    private fun String?.normalized(): String? = this?.trim()?.takeUnless {
        it.isEmpty() || it.equals("null", true) || it.equals("waiting", true) || it == "-1000" || it == "-1001"
    }

    private fun Throwable.rootCauseName(): String {
        var cause = this
        while (cause.cause != null && cause.cause !== cause) cause = cause.cause!!
        return cause.javaClass.simpleName.ifBlank { "UnknownError" }
    }

    private class DataChangedBinder(
        private val callback: (String?, String?) -> Unit,
    ) : Binder(), IInterface {
        init { attachInterface(this, DESCRIPTOR) }
        override fun asBinder(): IBinder = this
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code == INTERFACE_TRANSACTION) {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            if (code != TRANSACTION_CHANGED) return super.onTransact(code, data, reply, flags)
            data.enforceInterface(DESCRIPTOR)
            callback(data.readString(), data.readString())
            return true
        }

        private companion object {
            const val DESCRIPTOR = "com.gwm.android.adapter.IDataChangedListener"
            const val TRANSACTION_CHANGED = 1
        }
    }

    private companion object {
        const val SERVICE_PACKAGE = "com.gwm.android.adapter.server"
        const val SERVICE_CLASS = "com.gwm.android.adapter.server.GwmAdapterService"
        const val SERVICE_DESCRIPTOR = "com.gwm.android.adapter.IGwmAdapterService"
        const val TRANSACTION_GET_DATA = 1
        const val TRANSACTION_REGISTER_LISTENER = 3
        const val TRANSACTION_UNREGISTER_LISTENER = 4
        const val POLL_INTERVAL_MS = 1_000L
        const val LISTENER_SAFETY_POLL_MS = 5_000L
        const val STALE_AFTER_MS = 7_000L

        const val INDEX_SPEED = 0
        const val INDEX_RPM = 1
        const val INDEX_ODOMETER = 2
        const val INDEX_CURRENT_TRIP = 3
        const val INDEX_ACCUMULATED_TRIP = 4
        const val INDEX_FUEL_PERCENT = 5
        const val INDEX_RANGE = 6
        const val INDEX_COOLANT = 7
        const val INDEX_OUTSIDE_TEMP = 8
        const val INDEX_GEAR_STATUS = 9
        const val INDEX_CURRENT_GEAR = 10
        const val INDEX_TPMS = 11
        const val INDEX_AVERAGE_CONSUMPTION = 13
        const val INDEX_CURRENT_AVERAGE_CONSUMPTION_A = 14
        const val INDEX_BATTERY_VOLTAGE = 15
        const val INDEX_STEERING_ANGLE = 17
        const val INDEX_WHEEL_FRONT_LEFT = 18
        const val INDEX_WHEEL_FRONT_RIGHT = 19
        const val INDEX_WHEEL_REAR_LEFT = 20
        const val INDEX_WHEEL_REAR_RIGHT = 21
        const val INDEX_ENGINE_TORQUE = 22
        const val INDEX_DOOR_STATUS = 23
        const val INDEX_INSTANT_CONSUMPTION = 25
        const val INDEX_CURRENT_AVERAGE_CONSUMPTION_ALIAS = 26
        const val INDEX_TRANSMISSION_TEMP = 27
        const val INDEX_DRIVE_MODE = 28
        const val INDEX_OFFROAD_DRIVE_MODE = 29
        const val INDEX_EXPERT_DRIVE_MODE = 30
        const val INDEX_EXPERT_FOUR_WHEEL_DRIVE = 31
        const val INDEX_FRONT_DIFFERENTIAL_LOCK = 32
        const val INDEX_REAR_DIFFERENTIAL_LOCK = 33
        const val INDEX_AVERAGE_SPEED_SINCE_RESET = 34
        const val INDEX_AVERAGE_SPEED_SINCE_STARTUP = 35

        val DATA_IDS = arrayOf(
            "car.basic.vehicle_speed",
            "car.basic.engine_speed",
            "car.basic.total_odometer",
            "car.basic.cur_journey_odometer",
            "car.basic.accumulated_odometer",
            "car.basic.remain_fuel_percentage",
            "car.basic.remain_odometer",
            "car.basic.coolant_temp",
            "car.basic.outside_temp",
            "car.basic.gear_status",
            "car.basic.current_gear",
            "car.basic.tpms_status",
            "car.basic.tpms_units",
            "car.basic.avg_fuel_consumption",
            "car.basic.cur_journey_avg_fuel_consumption_a",
            "car.basic.battery_voltage",
            "car.basic.tire_temp_unit",
            "car.basic.steering_wheel_angle",
            "car.basic.fl_wheel_speed",
            "car.basic.fr_wheel_speed",
            "car.basic.rl_wheel_speed",
            "car.basic.rr_wheel_speed",
            "car.off_road_info.engine_flywheel_torque",
            "car.basic.door_status",
            "car.ipk_info.tts_contents",
            "car.basic.instant_fuel_consumption",
            "car.basic.cur_journey_avg_fuel_consume",
            "car.basic.transmission_oil_temp",
            "car.drive_setting.drive_mode",
            "car.off_road_setting.drive_mode_config",
            "car.off_road_setting.expert_mode_drive_mode",
            "car.off_road_setting.expert_mode_fwd",
            "car.ipk_light.front_differential_lock_indicator",
            "car.ipk_light.rear_differential_lock_indicator",
            "car.basic.vehicle_speed_since_reset",
            "car.basic.avg_vehicle_speed_since_startup",
        )
    }
}
