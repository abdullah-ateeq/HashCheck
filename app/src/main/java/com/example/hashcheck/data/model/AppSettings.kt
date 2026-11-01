package com.example.hashcheck.data.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Persisted application settings.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val uppercaseHex: Boolean = false,
    val autoCalculate: Boolean = true,
    val showCharByteCounts: Boolean = true,
    val clearOnLeave: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val enabledAlgorithms: Set<HashAlgorithm> = HashAlgorithm.DEFAULT_SELECTED
)
