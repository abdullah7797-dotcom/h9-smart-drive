package com.h9smartdrive.app.data.vehicle

import android.content.Context
import android.content.pm.PackageManager
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
import java.lang.reflect.InvocationTargetException
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Best-effort read-only bridge to the public Android Automotive property API.
 *
 * Reflection keeps the APK installable on non-AAOS Android 9 builds where android.car.jar is not
 * present. All unsupported and permission-protected properties remain unavailable instead of being
 * replaced with synthetic zeroes.
 */
class AndroidAutomotiveDataProvider(private val context: Context) : VehicleDataProvider {
    override val source = DataSource.ANDROID_AUTOMOTIVE
    private val _status = MutableStateFlow(probe())
    override val status: Flow<ProviderStatus> = _status
    private val _snapshots = MutableStateFlow(VehicleSnapshot())
    override val snapshots: Flow<VehicleSnapshot> = _snapshots

    private val providerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null
    private var bridge: AutomotiveBridge? = null

    override suspend fun start() {
        if (pollingJob != null) return
        val initial = probe()
        _status.value = initial
        if (!initial.available) return

        pollingJob = providerScope.launch {
            val candidate = AutomotiveBridge(context)
            bridge = candidate
            if (!candidate.connect()) {
                _status.value = ProviderStatus(true, false, candidate.lastError ?: "تعذر فتح CarPropertyManager")
                return@launch
            }
            _status.value = ProviderStatus(true, false, "CarPropertyManager متاح؛ جارٍ اختبار صلاحيات القراءة")
            while (isActive) {
                val snapshot = candidate.readSnapshot()
                _snapshots.value = snapshot
                val count = snapshot.availableReadingCount()
                _status.value = if (count > 0) {
                    ProviderStatus(true, true, "منصة 2025 متصلة للقراءة فقط · $count إشارات")
                } else {
                    ProviderStatus(
                        true,
                        false,
                        candidate.lastError ?: "Car Service موجود لكن خصائص السيارة محجوبة عن التطبيقات الجانبية",
                    )
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    override suspend fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        bridge?.close()
        bridge = null
        providerScope.cancel()
        _status.value = _status.value.copy(connected = false, detail = "تم إيقاف Android Automotive")
    }

    private fun probe(): ProviderStatus {
        val feature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
        val api = runCatching { Class.forName("android.car.Car") }.isSuccess
        val beanTechs = runCatching { context.packageManager.getPackageInfo(BEANTECHS_ADAPTER, 0) }.isSuccess
        val available = feature || api || beanTechs
        val detail = when {
            beanTechs && api -> "تم اكتشاف منصة GWM/BeanTechs وواجهة Car API"
            beanTechs -> "تم اكتشاف منصة GWM/BeanTechs؛ واجهة Car API غير ظاهرة"
            feature || api -> "تم اكتشاف Android Automotive"
            else -> "لم تُكتشف منصة Android Automotive أو BeanTechs"
        }
        return ProviderStatus(available, false, detail)
    }

    private class AutomotiveBridge(private val context: Context) {
        private var car: Any? = null
        private var propertyManager: Any? = null
        private var getPropertyMethod: java.lang.reflect.Method? = null
        private var getConfigMethod: java.lang.reflect.Method? = null
        var lastError: String? = null
            private set

        private val propertyIds = PropertyIds()
        private val wheelAreas = WheelAreas()

        fun connect(): Boolean = try {
            val carClass = Class.forName("android.car.Car")
            val createCar = carClass.methods.firstOrNull { method ->
                method.name == "createCar" && method.parameterTypes.size == 1 &&
                    Context::class.java.isAssignableFrom(method.parameterTypes[0])
            } ?: error("createCar غير متاح")
            val candidateCar = createCar.invoke(null, context) ?: error("Car API أعاد قيمة فارغة")
            // createCar(Context) is synchronous and returns an already connected instance on
            // Android Automotive 9. Calling connect() again throws "already connected".
            val manager = candidateCar.javaClass.getMethod("getCarManager", String::class.java)
                .invoke(candidateCar, "property") ?: error("خدمة property غير متاحة")
            val getProperty = manager.javaClass.methods.firstOrNull { method ->
                method.name == "getProperty" && method.parameterTypes.size == 3 &&
                    method.parameterTypes[0] == Class::class.java
            } ?: error("واجهة getProperty غير متاحة")
            car = candidateCar
            propertyManager = manager
            getPropertyMethod = getProperty
            getConfigMethod = manager.javaClass.methods.firstOrNull {
                it.name == "getCarPropertyConfig" && it.parameterTypes.size == 1
            }
            lastError = null
            true
        } catch (error: Throwable) {
            lastError = "تعذر فتح Car API: ${error.rootCauseName()}"
            false
        }

        fun close() {
            runCatching { car?.javaClass?.getMethod("disconnect")?.invoke(car) }
            car = null
            propertyManager = null
            getPropertyMethod = null
            getConfigMethod = null
        }

        fun readSnapshot(): VehicleSnapshot {
            val now = System.currentTimeMillis()
            var denied = false
            fun readFloat(id: Int?, area: Int = 0): Double? {
                if (id == null) return null
                return readNumber(id, area, java.lang.Float::class.java).also { if (it == null && isDeniedError()) denied = true }
            }
            fun readInt(id: Int?, area: Int = 0): Int? {
                if (id == null) return null
                return readNumber(id, area, java.lang.Integer::class.java)?.roundToInt().also { if (it == null && isDeniedError()) denied = true }
            }
            fun <T> reading(value: T?) = Reading(value, DataSource.ANDROID_AUTOMOTIVE, now, STALE_AFTER_MS)

            val speedMetersPerSecond = readFloat(propertyIds.speedDisplay) ?: readFloat(propertyIds.speed)
            val fuelLevel = readFloat(propertyIds.fuelLevel)
            val fuelCapacity = readFloat(propertyIds.fuelCapacity)
            val fuelPercent = if (fuelLevel != null && fuelCapacity != null && fuelCapacity > 0.0) {
                (fuelLevel / fuelCapacity * 100.0).coerceIn(0.0, 100.0).roundToInt()
            } else null
            val gearRaw = readInt(propertyIds.gearSelection)

            val tire = propertyIds.tirePressure
            val tires = TireState(
                frontLeftPsi = reading(readFloat(tire, wheelAreas.frontLeft)?.kpaToPsi()),
                frontRightPsi = reading(readFloat(tire, wheelAreas.frontRight)?.kpaToPsi()),
                rearLeftPsi = reading(readFloat(tire, wheelAreas.rearLeft)?.kpaToPsi()),
                rearRightPsi = reading(readFloat(tire, wheelAreas.rearRight)?.kpaToPsi()),
            )

            val snapshot = VehicleSnapshot(
                speedKph = reading(speedMetersPerSecond?.absoluteValue?.times(3.6)?.takeIf { it <= 300.0 }),
                rpm = reading(readFloat(propertyIds.engineRpm)?.takeIf { it in 0.0..9_000.0 }?.roundToInt()),
                fuelPercent = reading(fuelPercent),
                rangeKm = reading(readFloat(propertyIds.rangeRemaining)?.div(1_000.0)?.takeIf { it in 0.0..3_000.0 }?.roundToInt()),
                engineTempC = reading(readFloat(propertyIds.coolantTemperature)?.takeIf { it in -40.0..180.0 }?.roundToInt()),
                outsideTempC = reading(readFloat(propertyIds.outsideTemperature)?.takeIf { it in -60.0..80.0 }),
                odometerKm = reading(readFloat(propertyIds.odometer)?.div(1_000.0)?.takeIf { it in 0.0..2_000_000.0 }),
                gear = reading(propertyIds.gearLabel(gearRaw)),
                tires = tires,
            )
            if (snapshot.availableReadingCount() == 0 && denied) {
                lastError = "Car Service رفض الصلاحيات؛ التقرير سيحدد إن كان Shizuku مطلوبًا"
            } else if (snapshot.availableReadingCount() > 0) {
                lastError = null
            }
            return snapshot
        }

        private fun readNumber(propertyId: Int, areaId: Int, type: Class<*>): Double? {
            val manager = propertyManager ?: return null
            val method = getPropertyMethod ?: return null
            return try {
                val result = method.invoke(manager, type, propertyId, areaId)
                val value = if (result is Number) result else {
                    result?.javaClass?.methods?.firstOrNull { it.name == "getValue" && it.parameterCount == 0 }?.invoke(result)
                }
                (value as? Number)?.toDouble()?.takeIf { it.isFinite() }
            } catch (error: Throwable) {
                lastError = "تعذر قراءة خصائص السيارة: ${error.rootCauseName()}"
                null
            }
        }

        private fun isDeniedError(): Boolean = lastError?.contains("Security", ignoreCase = true) == true ||
            lastError?.contains("AccessDenied", ignoreCase = true) == true

        private fun Double.kpaToPsi(): Double? = takeIf { it in 50.0..600.0 }?.times(0.145_037_737_7)

        private class PropertyIds {
            private val constants = runCatching { Class.forName("android.car.VehiclePropertyIds") }.getOrNull()
            val speed = id("PERF_VEHICLE_SPEED", 291_504_647)
            val speedDisplay = id("PERF_VEHICLE_SPEED_DISPLAY", 291_504_648)
            val engineRpm = id("ENGINE_RPM", 291_504_901)
            val fuelLevel = id("FUEL_LEVEL", 291_504_903)
            val fuelCapacity = id("INFO_FUEL_CAPACITY", 291_504_388)
            val rangeRemaining = id("RANGE_REMAINING", 291_504_904)
            val coolantTemperature = id("ENGINE_COOLANT_TEMP", 291_504_897)
            val outsideTemperature = id("ENV_OUTSIDE_TEMPERATURE", 291_505_923)
            val odometer = id("PERF_ODOMETER", 291_504_644) ?: id("ODOMETER", null)
            val gearSelection = id("GEAR_SELECTION", 289_408_000)
            val tirePressure = id("TIRE_PRESSURE", 392_168_201)

            private val gearClass = runCatching { Class.forName("android.car.VehicleGear") }.getOrNull()
            private val park = gear("GEAR_PARK")
            private val reverse = gear("GEAR_REVERSE")
            private val neutral = gear("GEAR_NEUTRAL")
            private val drive = gear("GEAR_DRIVE")

            private fun id(name: String, fallback: Int?): Int? =
                runCatching { constants?.getField(name)?.getInt(null) }.getOrNull() ?: fallback

            private fun gear(name: String): Int? = runCatching { gearClass?.getField(name)?.getInt(null) }.getOrNull()

            fun gearLabel(value: Int?): String? = when (value) {
                null -> null
                park -> "P"
                reverse -> "R"
                neutral -> "N"
                drive -> "D"
                else -> null
            }
        }

        private class WheelAreas {
            private val constants = runCatching { Class.forName("android.car.VehicleAreaWheel") }.getOrNull()
            val frontLeft = area("WHEEL_LEFT_FRONT", 0x1)
            val frontRight = area("WHEEL_RIGHT_FRONT", 0x2)
            val rearLeft = area("WHEEL_LEFT_REAR", 0x4)
            val rearRight = area("WHEEL_RIGHT_REAR", 0x8)
            private fun area(name: String, fallback: Int): Int =
                runCatching { constants?.getField(name)?.getInt(null) }.getOrNull() ?: fallback
        }

        private fun Throwable.rootCauseName(): String {
            var unwrapped = if (this is InvocationTargetException) targetException ?: this else this
            while (unwrapped.cause != null && unwrapped.cause !== unwrapped) {
                unwrapped = unwrapped.cause!!
            }
            val name = unwrapped.javaClass.simpleName.ifBlank { "UnknownError" }
            val detail = unwrapped.message?.take(80)?.takeIf(String::isNotBlank)
            return if (detail == null) name else "$name: $detail"
        }
    }

    private companion object {
        const val BEANTECHS_ADAPTER = "com.beantechs.adapterservice.server"
        const val POLL_INTERVAL_MS = 1_000L
        const val STALE_AFTER_MS = 3_000L
    }
}

private fun VehicleSnapshot.availableReadingCount(): Int = listOf(
    speedKph.value,
    rpm.value,
    fuelPercent.value,
    rangeKm.value,
    engineTempC.value,
    outsideTempC.value,
    odometerKm.value,
    gear.value,
    tires.frontLeftPsi.value,
    tires.frontRightPsi.value,
    tires.rearLeftPsi.value,
    tires.rearRightPsi.value,
).count { it != null }
