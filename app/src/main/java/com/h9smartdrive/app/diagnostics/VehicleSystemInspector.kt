package com.h9smartdrive.app.diagnostics

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.h9smartdrive.app.BuildConfig
import com.h9smartdrive.app.data.vehicle.DataSource
import com.h9smartdrive.app.data.vehicle.ProviderStatus
import com.h9smartdrive.app.data.vehicle.Reading
import com.h9smartdrive.app.data.vehicle.VehicleSnapshot

data class ServiceProbe(
    val name: String,
    val exported: Boolean,
    val permission: String?,
)

data class PackageProbe(
    val roleAr: String,
    val packageName: String,
    val installed: Boolean,
    val versionName: String? = null,
    val services: List<ServiceProbe> = emptyList(),
)

data class SystemDiagnostics(
    val androidVersion: String,
    val sdk: Int,
    val manufacturer: String,
    val model: String,
    val product: String,
    val device: String,
    val buildDisplay: String,
    val processor: String,
    val screen: String,
    val automotive: Boolean,
    val carApiAvailable: Boolean,
    val gps: Boolean,
    val locationPermission: Boolean,
    val mapsHandler: Boolean,
    val mediaHandler: Boolean,
    val knownPackages: List<PackageProbe>,
    val vehiclePermissions: Map<String, Boolean>,
) {
    val installedVehiclePackages: List<PackageProbe> get() = knownPackages.filter(PackageProbe::installed)
    val grantedVehiclePermissionCount: Int get() = vehiclePermissions.count { it.value }
    val gwmPackageVisible: Boolean get() = knownPackages.any {
        it.packageName == "com.gwm.android.adapter.server" && it.installed
    }

    fun asJson(statuses: Map<DataSource, ProviderStatus>, vehicle: VehicleSnapshot): String = buildString {
        appendLine("{")
        appendLine("  \"schema\": 2,")
        appendLine("  \"report\": \"H9 Smart Drive 2025 compatibility\",")
        appendLine("  \"appVersion\": \"${BuildConfig.VERSION_NAME.escape()}\",")
        appendLine("  \"androidVersion\": \"${androidVersion.escape()}\",")
        appendLine("  \"sdk\": $sdk,")
        appendLine("  \"manufacturer\": \"${manufacturer.escape()}\",")
        appendLine("  \"model\": \"${model.escape()}\",")
        appendLine("  \"product\": \"${product.escape()}\",")
        appendLine("  \"device\": \"${device.escape()}\",")
        appendLine("  \"buildDisplay\": \"${buildDisplay.escape()}\",")
        appendLine("  \"processor\": \"${processor.escape()}\",")
        appendLine("  \"screen\": \"${screen.escape()}\",")
        appendLine("  \"automotiveFeature\": $automotive,")
        appendLine("  \"carApiAvailable\": $carApiAvailable,")
        appendLine("  \"gps\": $gps,")
        appendLine("  \"locationPermission\": $locationPermission,")
        appendLine("  \"packages\": [")
        append(knownPackages.joinToString(",\n") { pkg ->
            val services = pkg.services.joinToString(",") { service ->
                "{\"name\":\"${service.name.escape()}\",\"exported\":${service.exported},\"permission\":${service.permission.jsonStringOrNull()}}"
            }
            "    {\"role\":\"${pkg.roleAr.escape()}\",\"package\":\"${pkg.packageName.escape()}\",\"installed\":${pkg.installed},\"version\":${pkg.versionName.jsonStringOrNull()},\"services\":[$services]}"
        })
        appendLine()
        appendLine("  ],")
        appendLine("  \"permissions\": {")
        append(vehiclePermissions.entries.joinToString(",\n") { (name, granted) ->
            "    \"${name.escape()}\": $granted"
        })
        appendLine()
        appendLine("  },")
        appendLine("  \"providers\": {")
        append(statuses.entries.joinToString(",\n") { (source, status) ->
            "    \"${source.name}\": {\"available\": ${status.available}, \"connected\": ${status.connected}, \"detail\": \"${status.detail.escape()}\"}"
        })
        appendLine()
        appendLine("  },")
        appendLine("  \"signals\": {")
        append(signalAvailability(vehicle).entries.joinToString(",\n") { (name, reading) ->
            "    \"${name.escape()}\": {\"available\": ${reading.isAvailable()}, \"source\": \"${reading.source.name}\"}"
        })
        appendLine()
        appendLine("  },")
        appendLine("  \"privacy\": \"No VIN, coordinates, account identifiers, or full package inventory included\"")
        appendLine("}")
    }

    private fun String.escape() = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    private fun String?.jsonStringOrNull(): String = this?.let { "\"${it.escape()}\"" } ?: "null"

    private fun signalAvailability(vehicle: VehicleSnapshot): Map<String, Reading<*>> = linkedMapOf(
        "speedKph" to vehicle.speedKph,
        "rpm" to vehicle.rpm,
        "odometerKm" to vehicle.odometerKm,
        "currentTripKm" to vehicle.currentTripKm,
        "accumulatedTripKm" to vehicle.accumulatedTripKm,
        "averageSpeedKph" to vehicle.averageSpeedKph,
        "fuelPercent" to vehicle.fuelPercent,
        "rangeKm" to vehicle.rangeKm,
        "averageFuelConsumptionL100Km" to vehicle.averageFuelConsumptionL100Km,
        "instantFuelConsumptionL100Km" to vehicle.instantFuelConsumptionL100Km,
        "engineTempC" to vehicle.engineTempC,
        "transmissionTempC" to vehicle.transmissionTempC,
        "outsideTempC" to vehicle.outsideTempC,
        "batteryVoltage" to vehicle.batteryVoltage,
        "gear" to vehicle.gear,
        "driveMode" to vehicle.driveMode,
        "fourWheelDrive" to vehicle.fourWheelDrive,
        "frontDifferentialLocked" to vehicle.frontDifferentialLocked,
        "rearDifferentialLocked" to vehicle.rearDifferentialLocked,
        "doorStatus" to vehicle.doorStatus,
        "steeringAngleDeg" to vehicle.steeringAngleDeg,
        "engineTorqueNm" to vehicle.engineTorqueNm,
        "tireFrontLeftPsi" to vehicle.tires.frontLeftPsi,
        "tireFrontRightPsi" to vehicle.tires.frontRightPsi,
        "tireRearLeftPsi" to vehicle.tires.rearLeftPsi,
        "tireRearRightPsi" to vehicle.tires.rearRightPsi,
        "tireFrontLeftTempC" to vehicle.tires.frontLeftTempC,
        "tireFrontRightTempC" to vehicle.tires.frontRightTempC,
        "tireRearLeftTempC" to vehicle.tires.rearLeftTempC,
        "tireRearRightTempC" to vehicle.tires.rearRightTempC,
    )
}

class VehicleSystemInspector(private val context: Context) {
    fun inspect(): SystemDiagnostics {
        val pm = context.packageManager
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            .getRealMetrics(displayMetrics)

        val mapIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=Riyadh"))
        val mediaIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
        return SystemDiagnostics(
            androidVersion = Build.VERSION.RELEASE ?: "unknown",
            sdk = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER ?: "unknown",
            model = Build.MODEL ?: "unknown",
            product = Build.PRODUCT ?: "unknown",
            device = Build.DEVICE ?: "unknown",
            buildDisplay = Build.DISPLAY ?: "unknown",
            processor = Build.SUPPORTED_ABIS.joinToString(),
            screen = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels} @ ${displayMetrics.densityDpi}dpi",
            automotive = pm.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE),
            carApiAvailable = runCatching { Class.forName("android.car.Car") }.isSuccess,
            gps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
            locationPermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION),
            mapsHandler = mapIntent.resolveActivity(pm) != null,
            mediaHandler = mediaIntent.resolveActivity(pm) != null,
            knownPackages = KNOWN_PACKAGES.map { (role, packageName) -> inspectPackage(role, packageName) },
            vehiclePermissions = VEHICLE_PERMISSIONS.associateWith(::hasPermission),
        )
    }

    private fun hasPermission(permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    @Suppress("DEPRECATION")
    private fun inspectPackage(roleAr: String, packageName: String): PackageProbe = runCatching {
        val info = context.packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SERVICES or PackageManager.GET_PERMISSIONS,
        )
        PackageProbe(
            roleAr = roleAr,
            packageName = packageName,
            installed = true,
            versionName = info.versionName,
            services = info.services.orEmpty().take(MAX_SERVICES_PER_PACKAGE).map { service ->
                ServiceProbe(
                    name = service.name,
                    exported = service.exported,
                    permission = service.permission,
                )
            },
        )
    }.getOrElse {
        PackageProbe(roleAr = roleAr, packageName = packageName, installed = false)
    }

    private companion object {
        const val MAX_SERVICES_PER_PACKAGE = 16
        val KNOWN_PACKAGES = listOf(
            "محوّل GWM القديم" to "com.gwm.android.adapter.server",
            "محوّل BeanTechs لمنصة 2025" to "com.beantechs.adapterservice.server",
            "خدمة إدخال BeanTechs" to "com.beantechs.inputservice",
            "Android Car Service" to "com.android.car",
            "خدمة النظام الأساسية" to "com.core.system.service",
            "بوابة SOME/IP" to "com.autolink.car.someip.service",
            "خدمة TBOX" to "com.autolink.tboxservice",
            "خدمة شاشة العدادات" to "com.autolink.clusterservice",
        )
        val VEHICLE_PERMISSIONS = listOf(
            "com.gwm.android.adapter.permission.GET_DATA",
            "android.car.permission.CAR_SPEED",
            "android.car.permission.CAR_ENGINE_DETAILED_3P",
            "android.car.permission.CAR_ENGINE_DETAILED",
            "android.car.permission.CAR_ENERGY",
            "android.car.permission.CAR_EXTERIOR_ENVIRONMENT",
            "android.car.permission.CAR_TIRES_3P",
            "android.car.permission.CAR_TIRES",
            "android.car.permission.CAR_MILEAGE_3P",
            "android.car.permission.CAR_MILEAGE",
            "android.car.permission.CAR_POWERTRAIN",
            "android.car.permission.CAR_INFO",
        )
    }
}
