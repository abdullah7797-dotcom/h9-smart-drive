package com.h9smartdrive.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.h9smartdrive.app.data.local.UserPreferences
import com.h9smartdrive.app.data.local.UserSettings
import com.h9smartdrive.app.data.update.AppUpdateManager
import com.h9smartdrive.app.data.update.AppUpdateState
import com.h9smartdrive.app.data.vehicle.VehicleDataManager
import com.h9smartdrive.app.data.vehicle.VehicleSnapshot
import com.h9smartdrive.app.diagnostics.SystemDiagnostics
import com.h9smartdrive.app.diagnostics.VehicleSystemInspector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class H9UiState(
    val vehicle: VehicleSnapshot = VehicleSnapshot(),
    val settings: UserSettings = UserSettings(),
    val diagnostics: SystemDiagnostics? = null,
    val update: AppUpdateState = AppUpdateState(),
)

class H9ViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferences(application)
    private val vehicleDataManager = VehicleDataManager(application, viewModelScope)
    private val inspector = VehicleSystemInspector(application)
    private val updateManager = AppUpdateManager(application)

    val uiState: StateFlow<H9UiState> = combine(
        vehicleDataManager.snapshot,
        preferences.settings,
        updateManager.state,
    ) { vehicle, settings, update ->
        vehicleDataManager.setDemoMode(settings.demoMode)
        H9UiState(vehicle, settings, inspector.inspect(), update)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), H9UiState())

    init {
        viewModelScope.launch {
            delay(1_500)
            while (isActive) {
                updateManager.checkForUpdate()
                delay(UPDATE_CHECK_INTERVAL_MS)
            }
        }
    }

    fun setDemoMode(enabled: Boolean) = viewModelScope.launch { preferences.setDemoMode(enabled) }
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { preferences.setDarkMode(enabled) }
    fun setLanguage(language: String) = viewModelScope.launch { preferences.setLanguage(language) }
    fun setDashboardStyle(style: String) = viewModelScope.launch { preferences.setStyle(style) }
    fun checkForUpdate() = viewModelScope.launch { updateManager.checkForUpdate(manual = true) }
    fun downloadUpdate() = viewModelScope.launch { updateManager.downloadUpdate() }
    fun installUpdate() = updateManager.installDownloadedUpdate()
    fun clearUpdateMessage() = updateManager.clearMessage()

    private companion object {
        const val UPDATE_CHECK_INTERVAL_MS = 6 * 60 * 60 * 1_000L
    }
}
