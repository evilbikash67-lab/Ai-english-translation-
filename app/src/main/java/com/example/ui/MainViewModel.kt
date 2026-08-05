package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Language
import com.example.api.TranslationResult
import com.example.data.ChatSessionEntity
import com.example.data.TranslationEntity
import com.example.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    val repository = TranslationRepository(application)

    val historyList: StateFlow<List<TranslationEntity>> = repository.allTranslations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteList: StateFlow<List<TranslationEntity>> = repository.favoriteTranslations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTranslationsCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTokensCount: StateFlow<Int?> = repository.totalTokensUsed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val chatMessages: StateFlow<List<ChatSessionEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State Variables ---
    private val _sourceLanguage = MutableStateFlow(Language.AUTO)
    val sourceLanguage = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(Language.SUPPORTED_LANGUAGES[1]) // English
    val targetLanguage = _targetLanguage.asStateFlow()

    private val _selectedTone = MutableStateFlow<String?>(null)
    val selectedTone = _selectedTone.asStateFlow()

    private val _preferredProvider = MutableStateFlow("gemini") // "gemini" or "openrouter"
    val preferredProvider = _preferredProvider.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _translationResult = MutableStateFlow<TranslationResult?>(null)
    val translationResult = _translationResult.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating = _isTranslating.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput = _chatInput.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Text to Speech Engine
    private var tts: TextToSpeech? = TextToSpeech(application, this)
    private var isTtsReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsReady = true
        }
    }

    fun setSourceLanguage(language: Language) {
        _sourceLanguage.value = language
    }

    fun setTargetLanguage(language: Language) {
        _targetLanguage.value = language
    }

    fun setSelectedTone(tone: String?) {
        _selectedTone.value = tone
    }

    fun setPreferredProvider(provider: String) {
        _preferredProvider.value = provider
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setChatInput(text: String) {
        _chatInput.value = text
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun swapLanguages() {
        if (_sourceLanguage.value.code != "auto") {
            val temp = _sourceLanguage.value
            _sourceLanguage.value = _targetLanguage.value
            _targetLanguage.value = temp
        }
    }

    fun translateCurrentInput() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        _isTranslating.value = true
        viewModelScope.launch {
            val res = repository.translateText(
                text = text,
                sourceLangCode = _sourceLanguage.value.code,
                targetLangCode = _targetLanguage.value.code,
                toneStyle = _selectedTone.value,
                preferredProvider = _preferredProvider.value
            )
            _translationResult.value = res
            _isTranslating.value = false
        }
    }

    fun speakText(text: String, langCode: String = "en") {
        if (isTtsReady && text.isNotBlank()) {
            val locale = Locale(langCode)
            tts?.language = locale
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TranslationTTS")
        }
    }

    fun sendChatMessage() {
        val message = _chatInput.value.trim()
        if (message.isEmpty()) return

        _chatInput.value = ""
        _isChatLoading.value = true

        viewModelScope.launch {
            repository.sendChatMessage(message)
            _isChatLoading.value = false
        }
    }

    fun toggleFavorite(entity: TranslationEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(entity.id, entity.isFavorite)
        }
    }

    fun deleteTranslation(id: Long) {
        viewModelScope.launch {
            repository.deleteTranslation(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
