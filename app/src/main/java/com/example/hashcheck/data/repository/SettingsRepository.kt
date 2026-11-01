package com.example.hashcheck.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hashcheck.data.model.AppSettings
import com.example.hashcheck.data.model.HashAlgorithm
import com.example.hashcheck.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hashcheck_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val UPPERCASE_HEX = booleanPreferencesKey("uppercase_hex")
        val AUTO_CALCULATE = booleanPreferencesKey("auto_calculate")
        val SHOW_CHAR_BYTE_COUNTS = booleanPreferencesKey("show_char_byte_counts")
        val CLEAR_ON_LEAVE = booleanPreferencesKey("clear_on_leave")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ENABLED_ALGORITHMS = stringSetPreferencesKey("enabled_algorithms")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }

            val uppercaseHex = preferences[PreferencesKeys.UPPERCASE_HEX] ?: false
            val autoCalculate = preferences[PreferencesKeys.AUTO_CALCULATE] ?: true
            val showCharByteCounts = preferences[PreferencesKeys.SHOW_CHAR_BYTE_COUNTS] ?: true
            val clearOnLeave = preferences[PreferencesKeys.CLEAR_ON_LEAVE] ?: false
            val hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true

            val savedAlgoNames = preferences[PreferencesKeys.ENABLED_ALGORITHMS]
            val enabledAlgorithms = if (savedAlgoNames != null) {
                val parsed = savedAlgoNames.mapNotNull { HashAlgorithm.fromAlgorithmName(it) }.toSet()
                if (parsed.isEmpty()) HashAlgorithm.DEFAULT_SELECTED else parsed
            } else {
                HashAlgorithm.DEFAULT_SELECTED
            }

            AppSettings(
                themeMode = themeMode,
                uppercaseHex = uppercaseHex,
                autoCalculate = autoCalculate,
                showCharByteCounts = showCharByteCounts,
                clearOnLeave = clearOnLeave,
                hapticsEnabled = hapticsEnabled,
                enabledAlgorithms = enabledAlgorithms
            )
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setUppercaseHex(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UPPERCASE_HEX] = enabled
        }
    }

    suspend fun setAutoCalculate(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_CALCULATE] = enabled
        }
    }

    suspend fun setShowCharByteCounts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_CHAR_BYTE_COUNTS] = enabled
        }
    }

    suspend fun setClearOnLeave(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLEAR_ON_LEAVE] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setAlgorithmEnabled(algorithm: HashAlgorithm, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.ENABLED_ALGORITHMS] ?: HashAlgorithm.DEFAULT_SELECTED.map { it.name }.toSet()
            val updated = current.toMutableSet()
            if (enabled) {
                updated.add(algorithm.name)
            } else {
                // Keep at least one algorithm enabled
                if (updated.size > 1) {
                    updated.remove(algorithm.name)
                }
            }
            preferences[PreferencesKeys.ENABLED_ALGORITHMS] = updated
        }
    }

    suspend fun setAllAlgorithms(algorithms: Set<HashAlgorithm>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLED_ALGORITHMS] = algorithms.map { it.name }.toSet()
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
