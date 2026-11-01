package com.example.hashcheck.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashcheck.data.model.AppSettings
import com.example.hashcheck.data.model.HashAlgorithm
import com.example.hashcheck.data.model.ThemeMode
import com.example.hashcheck.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val repository: SettingsRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, SettingsRepository(application))

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setUppercaseHex(uppercase: Boolean) {
        viewModelScope.launch {
            repository.setUppercaseHex(uppercase)
        }
    }

    fun setAutoCalculate(auto: Boolean) {
        viewModelScope.launch {
            repository.setAutoCalculate(auto)
        }
    }

    fun setShowCharByteCounts(show: Boolean) {
        viewModelScope.launch {
            repository.setShowCharByteCounts(show)
        }
    }

    fun setClearOnLeave(clear: Boolean) {
        viewModelScope.launch {
            repository.setClearOnLeave(clear)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticsEnabled(enabled)
        }
    }

    fun toggleAlgorithm(algorithm: HashAlgorithm, enabled: Boolean) {
        viewModelScope.launch {
            repository.setAlgorithmEnabled(algorithm, enabled)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
        }
    }
}
