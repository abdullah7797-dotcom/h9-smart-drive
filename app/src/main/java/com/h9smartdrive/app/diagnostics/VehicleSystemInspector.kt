package com.h9smartdrive.app.diagnostics

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.h9smartdrive.app.data.vehicle.ProviderStatus

data class SystemDiagnostics(
    val androidVersion: String,
    val sdk: Int,
    val manufacturer: String,
    val model: String,
    val processor: String,
    val screen: String,
    val automotive: Boolean,
    val gps: Boolean,
    val locationPermission: Boolean,
    val mapsHandler: Boolean,
    val mediaHandler: Boolean,
    val gwmPackageVisible: Boolean,
) {
    fun asJson(statuses: Map<String, ProviderStatus>): String = buildString {
        appendLine("{")
        appendLine("  \"schema\": 1,")
        appendLine("  \"androidVersion\": \"$androidVersion\",")
        appendLine("  \"sdk\": $sdk,")
        appendLine("  \"manufacturer\": \"${manufacturer.escape()}\",")
        appendLine("  \"model\": \"${model.escape()}\",")
        appendLine("  \"processor\": \"${processor.escape()}\",")
        appendLine("  \"screen\": \"$screen\",")
        appendLine("  \"automotive\": $automotive,")
        appendLine("  \"gps\": $gps,")
        appendLine("  \"locationPermission\": $locationPermission,")
        appendLine("  \"mapsHandler\": $mapsHandler,")
        appendLine("  \"mediaHandler\": $mediaHandler,")
        appendLine("  \"gwmPackageVisible\": $gwmPackageVisible,")
        appendLine("  \"providers\": {")
        append(statuses.entries.joinToString(",\n") { (name, status) ->
            "    \"${name.escape()}\": {\"available\": ${status.available}, \"connected\": ${status.connected}, \"detail\": \"${status.detail.escape()}\"}"
        })
        appendLine()
        appendLine("  }")
        appendLine("}")
    }

    private fun String.escape() = replace("\\", "\\\\").replace("\"", "\\\"")
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
            processor = Build.SUPPORTED_ABIS.joinToString(),
            screen = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels} @ ${displayMetrics.densityDpi}dpi",
            automotive = pm.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE),
            gps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
            locationPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
            mapsHandler = mapIntent.resolveActivity(pm) != null,
            mediaHandler = mediaIntent.resolveActivity(pm) != null,
            gwmPackageVisible = runCatching { pm.getPackageInfo("com.gwm.android.adapter.server", 0) }.isSuccess,
        )
    }
}
