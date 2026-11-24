package com.h9smartdrive.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.h9smartdrive.app.data.local.UserPreferences
import com.h9smartdrive.app.data.local.UserSettings
import com.h9smartdrive.app.data.vehicle.VehicleDataManager
import com.h9smartdrive.app.data.vehicle.VehicleSnapshot
import com.h9smartdrive.app.diagnostics.SystemDiagnostics
import com.h9smartdrive.app.diagnostics.VehicleSystemInspector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class H9UiState(
    val vehicle: VehicleSnapshot = VehicleSnapshot(),
    val settings: UserSettings = UserSettings(),
    val diagnostics: SystemDiagnostics? = null,
)

class H9ViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferences(application)
    private val vehicleDataManager = VehicleDataManager(application, viewModelScope)
    private val inspector = VehicleSystemInspector(application)

    val uiState: StateFlow<H9UiState> = combine(
        vehicleDataManager.snapshot,
        preferences.settings,
    ) { vehicle, settings ->
        vehicleDataManager.setDemoMode(settings.demoMode)
        H9UiState(vehicle, settings, inspector.inspect())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), H9UiState())

    fun setDemoMode(enabled: Boolean) = viewModelScope.launch { preferences.setDemoMode(enabled) }
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { preferences.setDarkMode(enabled) }
    fun setLanguage(language: String) = viewModelScope.launch { preferences.setLanguage(language) }
    fun setDashboardStyle(style: String) = viewModelScope.launch { preferences.setStyle(style) }
}
