package com.h9smartdrive.app

import android.Manifest
import android.content.pm.PermissionInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.h9smartdrive.app.ui.H9SmartDriveApp
import com.h9smartdrive.app.ui.H9ViewModel

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: H9ViewModel = viewModel()
            H9SmartDriveApp(viewModel)
        }
        val requested = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            "android.car.permission.CAR_SPEED",
            "android.car.permission.CAR_ENGINE_DETAILED_3P",
            "android.car.permission.CAR_ENERGY",
            "android.car.permission.CAR_TIRES_3P",
            "android.car.permission.CAR_MILEAGE_3P",
        ).filter(::isRequestableAndMissing)
        if (requested.isNotEmpty()) {
            permissionLauncher.launch(requested.toTypedArray())
        }
    }

    @Suppress("DEPRECATION")
    private fun isRequestableAndMissing(permission: String): Boolean {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) return false
        val info = runCatching { packageManager.getPermissionInfo(permission, 0) }.getOrNull() ?: return false
        return info.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE == PermissionInfo.PROTECTION_DANGEROUS
    }
}
