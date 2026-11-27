package com.h9smartdrive.app.data.vehicle

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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
 * Read-only compatibility path for first-generation H9 head units.
 *
 * Only the factory adapter's documented GET_DATA transaction is used. No write transaction,
 * CAN command, diagnostic request, or vehicle setting is sent.
 */
class GwmVehicleDataProvider(private val context: Context) : VehicleDataProvider, ServiceConnection {
    override val source = DataSource.GWM
    private val _status = MutableStateFlow(probe())
    override val status: Flow<ProviderStatus> = _status
    private val _snapshots = MutableStateFlow(VehicleSnapshot())
    override val snapshots: Flow<VehicleSnapshot> = _snapshots

    private val providerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var service: IBinder? = null
    private var bound = false
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
        service = null
        if (bound) {
            withContext(Dispatchers.Main.immediate) { runCatching { context.unbindService(this@GwmVehicleDataProvider) } }
        }
        bound = false
        _status.value = _status.value.copy(connected = false, detail = "تم إيقاف موفّر GWM")
        providerScope.cancel()
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = binder
        _status.value = ProviderStatus(true, false, "تم ربط خدمة GWM؛ في انتظار أول قراءة")
        pollingJob?.cancel()
        pollingJob = providerScope.launch {
            while (isActive) {
                val result = readSnapshot(binder)
                if (result != null) {
                    val snapshot = result.toVehicleSnapshot()
                    _snapshots.value = snapshot
                    val liveCount = snapshot.availableReadingCount()
                    _status.value = if (liveCount > 0) {
                        ProviderStatus(true, true, "متصل للقراءة فقط · $liveCount إشارات صالحة")
                    } else {
                        ProviderStatus(true, false, "الخدمة متصلة لكن لم تُرجع أرقامًا صالحة")
                    }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        pollingJob?.cancel()
        pollingJob = null
        service = null
        _status.value = ProviderStatus(true, false, "انقطع اتصال خدمة GWM")
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
            detail = if (installed) "تم اكتشاف محوّل GWM القديم" else "محوّل GWM القديم غير موجود؛ سيتم تجربة منصة 2025",
        )
    }

    private fun readSnapshot(binder: IBinder): Array<String?>? {
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
            reply.createStringArray()?.map { it?.trim() }?.toTypedArray()
        } catch (error: Throwable) {
            _status.value = ProviderStatus(true, false, "رفض قراءة GWM: ${error.javaClass.simpleName}")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun Array<String?>.toVehicleSnapshot(): VehicleSnapshot {
        val now = System.currentTimeMillis()
        fun number(index: Int, min: Double, max: Double): Double? =
            getOrNull(index)?.toDoubleOrNull()?.takeIf { it.isFinite() && it in min..max }
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
                raw in 1..8 -> raw
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

        return VehicleSnapshot(
            speedKph = reading(number(INDEX_SPEED, 0.0, 260.0)),
            rpm = reading(number(INDEX_RPM, 0.0, 9_000.0)?.roundToInt()),
            odometerKm = reading(number(INDEX_ODOMETER, 0.0, 2_000_000.0)),
            fuelPercent = reading(number(INDEX_FUEL_PERCENT, 0.0, 100.0)?.roundToInt()),
            rangeKm = reading(number(INDEX_RANGE, 0.0, 3_000.0)?.roundToInt()),
            engineTempC = reading(number(INDEX_COOLANT, -40.0, 180.0)?.roundToInt()),
            outsideTempC = reading(number(INDEX_OUTSIDE_TEMP, -60.0, 80.0)),
            gear = reading(gear),
            batteryVoltage = reading(number(INDEX_BATTERY_VOLTAGE, 5.0, 20.0)),
        )
    }

    private fun VehicleSnapshot.availableReadingCount(): Int = listOf(
        speedKph.value,
        rpm.value,
        odometerKm.value,
        fuelPercent.value,
        rangeKm.value,
        engineTempC.value,
        outsideTempC.value,
        gear.value,
        batteryVoltage.value,
    ).count { it != null }

    private companion object {
        const val SERVICE_PACKAGE = "com.gwm.android.adapter.server"
        const val SERVICE_CLASS = "com.gwm.android.adapter.server.GwmAdapterService"
        const val SERVICE_DESCRIPTOR = "com.gwm.android.adapter.IGwmAdapterService"
        const val TRANSACTION_GET_DATA = 1
        const val POLL_INTERVAL_MS = 1_000L
        const val STALE_AFTER_MS = 3_000L

        const val INDEX_SPEED = 0
        const val INDEX_RPM = 1
        const val INDEX_ODOMETER = 2
        const val INDEX_FUEL_PERCENT = 3
        const val INDEX_RANGE = 4
        const val INDEX_COOLANT = 5
        const val INDEX_OUTSIDE_TEMP = 6
        const val INDEX_GEAR_STATUS = 7
        const val INDEX_CURRENT_GEAR = 8
        const val INDEX_BATTERY_VOLTAGE = 9

        val DATA_IDS = arrayOf(
            "car.basic.vehicle_speed",
            "car.basic.engine_speed",
            "car.basic.total_odometer",
            "car.basic.remain_fuel_percentage",
            "car.basic.remain_odometer",
            "car.basic.coolant_temp",
            "car.basic.outside_temp",
            "car.basic.gear_status",
            "car.basic.current_gear",
            "car.basic.battery_voltage",
        )
    }
}
