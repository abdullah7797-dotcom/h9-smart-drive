package com.h9smartdrive.app.data.vehicle

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ObdDataProvider : VehicleDataProvider {
    override val source = DataSource.OBD
    override val status: Flow<ProviderStatus> = MutableStateFlow(
        ProviderStatus(false, false, "OBD-II اختياري وغير مُهيأ"),
    )
    override val snapshots: Flow<VehicleSnapshot> = MutableStateFlow(VehicleSnapshot())
    override suspend fun start() = Unit
    override suspend fun stop() = Unit
}
