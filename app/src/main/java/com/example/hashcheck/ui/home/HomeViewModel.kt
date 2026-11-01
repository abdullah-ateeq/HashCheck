package com.example.hashcheck.ui.home

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashcheck.data.hash.HashEngine
import com.example.hashcheck.data.model.AppSettings
import com.example.hashcheck.data.model.FileInfo
import com.example.hashcheck.data.model.HashResult
import com.example.hashcheck.data.repository.SettingsRepository
import com.example.hashcheck.ui.components.HashMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, SettingsRepository(application))

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    private val _activeMode = MutableStateFlow(HashMode.TEXT)
    val activeMode: StateFlow<HashMode> = _activeMode.asStateFlow()

    // Text Hashing State
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _textResults = MutableStateFlow<List<HashResult>>(emptyList())
    val textResults: StateFlow<List<HashResult>> = _textResults.asStateFlow()

    private val _isCalculatingText = MutableStateFlow(false)
    val isCalculatingText: StateFlow<Boolean> = _isCalculatingText.asStateFlow()

    // File Hashing State
    private val _selectedFile = MutableStateFlow<FileInfo?>(null)
    val selectedFile: StateFlow<FileInfo?> = _selectedFile.asStateFlow()

    private val _fileResults = MutableStateFlow<List<HashResult>>(emptyList())
    val fileResults: StateFlow<List<HashResult>> = _fileResults.asStateFlow()

    private val _isFileHashing = MutableStateFlow(false)
    val isFileHashing: StateFlow<Boolean> = _isFileHashing.asStateFlow()

    private val _fileBytesProcessed = MutableStateFlow(0L)
    val fileBytesProcessed: StateFlow<Long> = _fileBytesProcessed.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var fileHashingJob: Job? = null
    private var textDebounceJob: Job? = null

    init {
        // Observe settings changes to recalculate if algorithm selection or casing changed
        viewModelScope.launch {
            settings.collect { currentSettings ->
                if (_inputText.value.isNotEmpty() && _textResults.value.isNotEmpty()) {
                    calculateTextHashesNow(currentSettings)
                }
            }
        }
    }

    fun setMode(mode: HashMode) {
        _activeMode.value = mode
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
        if (text.isEmpty()) {
            _textResults.value = emptyList()
            textDebounceJob?.cancel()
            return
        }

        if (settings.value.autoCalculate) {
            textDebounceJob?.cancel()
            textDebounceJob = viewModelScope.launch {
                kotlinx.coroutines.delay(180)
                calculateTextHashesNow(settings.value)
            }
        }
    }

    fun calculateTextHashes() {
        calculateTextHashesNow(settings.value)
    }

    private fun calculateTextHashesNow(currentSettings: AppSettings) {
        val text = _inputText.value
        if (text.isEmpty()) {
            _textResults.value = emptyList()
            return
        }

        _isCalculatingText.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val results = HashEngine.calculateTextHashes(
                text = text,
                algorithms = currentSettings.enabledAlgorithms,
                uppercase = currentSettings.uppercaseHex
            )
            withContext(Dispatchers.Main) {
                _textResults.value = results
                _isCalculatingText.value = false
            }
        }
    }

    fun clearTextInputAndResults() {
        textDebounceJob?.cancel()
        _inputText.value = ""
        _textResults.value = emptyList()
    }

    fun onFileSelected(uri: Uri) {
        fileHashingJob?.cancel()
        _fileResults.value = emptyList()
        _fileBytesProcessed.value = 0L
        _errorMessage.value = null

        val context = getApplication<Application>()
        var name = "Selected File"
        var size = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            // Fallback: uri last path segment
            name = uri.lastPathSegment ?: "Unknown File"
        }

        val mimeType = context.contentResolver.getType(uri)
        val fileInfo = FileInfo(
            uri = uri,
            name = name,
            sizeBytes = size,
            mimeType = mimeType
        )
        _selectedFile.value = fileInfo

        // Start hashing file immediately
        calculateFileHashes()
    }

    fun calculateFileHashes() {
        val fileInfo = _selectedFile.value ?: return
        val currentSettings = settings.value

        fileHashingJob?.cancel()
        _isFileHashing.value = true
        _fileBytesProcessed.value = 0L
        _fileResults.value = emptyList()
        _errorMessage.value = null

        fileHashingJob = viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                val inputStream = context.contentResolver.openInputStream(fileInfo.uri)
                if (inputStream == null) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Cannot access file: InputStream is null."
                        _isFileHashing.value = false
                    }
                    return@launch
                }

                val results = HashEngine.calculateStreamHashes(
                    inputStream = inputStream,
                    algorithms = currentSettings.enabledAlgorithms,
                    uppercase = currentSettings.uppercaseHex,
                    isCancelled = { !coroutineContext[Job]!!.isActive },
                    onProgress = { bytesRead ->
                        _fileBytesProcessed.value = bytesRead
                    }
                )

                withContext(Dispatchers.Main) {
                    _fileResults.value = results
                    _isFileHashing.value = false
                }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "File access denied: Permission missing or revoked."
                    _isFileHashing.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = e.localizedMessage ?: "Failed to read file."
                    _isFileHashing.value = false
                }
            }
        }
    }

    fun cancelFileHashing() {
        fileHashingJob?.cancel()
        _isFileHashing.value = false
    }

    fun clearFile() {
        cancelFileHashing()
        _selectedFile.value = null
        _fileResults.value = emptyList()
        _fileBytesProcessed.value = 0L
        _errorMessage.value = null
    }

    fun clearOnAppLeave() {
        if (settings.value.clearOnLeave) {
            clearTextInputAndResults()
            clearFile()
        }
    }

    fun copyAllResults(results: List<HashResult>): String {
        val maxNameLen = results.maxOfOrNull { it.algorithm.displayName.length } ?: 7
        return results.filter { it.isSuccess }.joinToString("\n") { result ->
            val paddedName = result.algorithm.displayName.padEnd(maxNameLen + 1)
            "$paddedName: ${result.hashHex}"
        }
    }
}
