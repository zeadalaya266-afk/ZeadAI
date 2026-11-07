package com.zead.ai.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zead.ai.R
import com.zead.ai.data.local.SecurePreferences
import com.zead.ai.data.remote.GeminiApiClient
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val securePreferences = SecurePreferences(application)
    private val geminiApiClient = GeminiApiClient()

    private val _uiState = MutableLiveData<SettingsUiState>(SettingsUiState.Idle)
    val uiState: LiveData<SettingsUiState> = _uiState

    fun loadSavedApiKey(): String {
        return securePreferences.getApiKey() ?: ""
    }

    fun saveApiKey(apiKey: String) {
        val app = getApplication<Application>()
        if (apiKey.isBlank()) {
            _uiState.value = SettingsUiState.Error(app.getString(R.string.error_key_empty))
            return
        }
        try {
            securePreferences.saveApiKey(apiKey.trim())
            _uiState.value = SettingsUiState.Saved
        } catch (e: Exception) {
            _uiState.value = SettingsUiState.Error(
                app.getString(R.string.error_save_failed, e.message ?: "Unknown")
            )
        }
    }

    fun testConnection(apiKey: String) {
        val app = getApplication<Application>()
        if (apiKey.isBlank()) {
            _uiState.value = SettingsUiState.Error(app.getString(R.string.error_key_empty))
            return
        }
        _uiState.value = SettingsUiState.Loading
        viewModelScope.launch {
            val result = geminiApiClient.testConnection(apiKey.trim())
            _uiState.value = result.fold(
                onSuccess = { SettingsUiState.Success },
                onFailure = {
                    SettingsUiState.Error(
                        app.getString(R.string.error_connection_failed, it.message ?: "Unknown")
                    )
                }
            )
        }
    }
}

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    object Saved : SettingsUiState()
    object Success : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}