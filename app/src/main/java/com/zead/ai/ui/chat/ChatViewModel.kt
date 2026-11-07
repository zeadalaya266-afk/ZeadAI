package com.zead.ai.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zead.ai.data.local.SecurePreferences
import com.zead.ai.data.remote.GeminiApiClient
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val securePreferences = SecurePreferences(application)
    private val geminiApiClient = GeminiApiClient()

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val apiKey = securePreferences.getApiKey()
        if (apiKey.isNullOrBlank()) {
            _errorMessage.value = "الرجاء إدخال مفتاح API في الإعدادات أولاً."
            return
        }

        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(ChatMessage(text = text, isUser = true))
        _messages.value = currentMessages

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = geminiApiClient.generateContent(apiKey, text)
            _isLoading.value = false
            
            result.fold(
                onSuccess = { responseText ->
                    val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
                    updatedMessages.add(ChatMessage(text = responseText, isUser = false))
                    _messages.value = updatedMessages
                },
                onFailure = { error ->
                    _errorMessage.value = "فشل الاتصال: ${error.message}"
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}