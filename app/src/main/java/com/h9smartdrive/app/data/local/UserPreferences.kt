package com.h9smartdrive.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("h9_preferences")

data class UserSettings(
    val demoMode: Boolean = true,
    val darkMode: Boolean = true,
    val language: String = "ar",
    val dashboardStyle: String = "modern",
)

class UserPreferences(private val context: Context) {
    private object Keys {
        val demo = booleanPreferencesKey("demo_mode")
        val dark = booleanPreferencesKey("dark_mode")
        val language = stringPreferencesKey("language")
        val style = stringPreferencesKey("dashboard_style")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { values ->
        UserSettings(
            demoMode = values[Keys.demo] ?: true,
            darkMode = values[Keys.dark] ?: true,
            language = values[Keys.language] ?: "ar",
            dashboardStyle = values[Keys.style] ?: "modern",
        )
    }

    suspend fun setDemoMode(enabled: Boolean) = context.dataStore.edit { it[Keys.demo] = enabled }
    suspend fun setDarkMode(enabled: Boolean) = context.dataStore.edit { it[Keys.dark] = enabled }
    suspend fun setLanguage(language: String) = context.dataStore.edit { it[Keys.language] = language }
    suspend fun setStyle(style: String) = context.dataStore.edit { it[Keys.style] = style }
}
