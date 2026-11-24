package com.h9smartdrive.app.data.vehicle

import kotlinx.coroutines.flow.Flow

interface VehicleDataProvider {
    val source: DataSource
    val status: Flow<ProviderStatus>
    val snapshots: Flow<VehicleSnapshot>
    suspend fun start()
    suspend fun stop()
}
