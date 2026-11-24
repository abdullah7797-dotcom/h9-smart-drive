package com.h9smartdrive.app.data.vehicle

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Read-only compatibility probes. Real binder/AIDL bindings are intentionally not guessed:
 * the exact contract must be captured and validated on the target head unit first.
 */
class GwmVehicleDataProvider(private val context: Context) : VehicleDataProvider {
    override val source = DataSource.GWM
    private val _status = MutableStateFlow(probe())
    override val status: Flow<ProviderStatus> = _status
    override val snapshots: Flow<VehicleSnapshot> = MutableStateFlow(VehicleSnapshot())

    private fun probe(): ProviderStatus {
        val packageFound = runCatching {
            context.packageManager.getPackageInfo("com.gwm.android.adapter.server", 0)
        }.isSuccess
        return ProviderStatus(
            available = packageFound,
            connected = false,
            detail = if (packageFound) {
                "تم اكتشاف حزمة GWM؛ يلزم التحقق من عقد Binder على السيارة"
            } else {
                "لم تُكتشف حزمة GWM المعلنة"
            },
        )
    }

    override suspend fun start() { _status.value = probe() }
    override suspend fun stop() { _status.value = _status.value.copy(connected = false) }
}

class AndroidAutomotiveDataProvider(private val context: Context) : VehicleDataProvider {
    override val source = DataSource.ANDROID_AUTOMOTIVE
    private val _status = MutableStateFlow(probe())
    override val status: Flow<ProviderStatus> = _status
    override val snapshots: Flow<VehicleSnapshot> = MutableStateFlow(VehicleSnapshot())

    private fun probe(): ProviderStatus {
        val automotive = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
        return ProviderStatus(
            available = automotive,
            connected = false,
            detail = if (automotive) "Android Automotive متاح؛ صلاحيات خصائص السيارة قيد الفحص" else "الجهاز لا يعلن Android Automotive",
        )
    }
    override suspend fun start() { _status.value = probe() }
    override suspend fun stop() { _status.value = _status.value.copy(connected = false) }
}

class ObdDataProvider : VehicleDataProvider {
    override val source = DataSource.OBD
    override val status: Flow<ProviderStatus> = MutableStateFlow(
        ProviderStatus(false, false, "OBD-II اختياري وغير مُهيأ"),
    )
    override val snapshots: Flow<VehicleSnapshot> = MutableStateFlow(VehicleSnapshot())
    override suspend fun start() = Unit
    override suspend fun stop() = Unit
}
