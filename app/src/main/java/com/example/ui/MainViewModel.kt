package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Language
import com.example.api.TranslationResult
import com.example.data.ChatSessionEntity
import com.example.data.ClipboardItem
import com.example.data.KeyboardMode
import com.example.data.KeyboardSettings
import com.example.data.KeyboardThemeId
import com.example.data.StickerPack
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
    private val _activeTheme = MutableStateFlow(KeyboardThemeId.GLASSMORPHISM)
    val activeTheme = _activeTheme.asStateFlow()

    private val _keyboardMode = MutableStateFlow(KeyboardMode.STANDARD)
    val keyboardMode = _keyboardMode.asStateFlow()

    private val _keyboardSettings = MutableStateFlow(KeyboardSettings())
    val keyboardSettings = _keyboardSettings.asStateFlow()

    private val _sourceLanguage = MutableStateFlow(Language.AUTO)
    val sourceLanguage = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(Language.SUPPORTED_LANGUAGES[1]) // English
    val targetLanguage = _targetLanguage.asStateFlow()

    private val _selectedTone = MutableStateFlow<String?>(null)
    val selectedTone = _selectedTone.asStateFlow()

    private val _preferredProvider = MutableStateFlow("openrouter") // "gemini" or "openrouter"
    val preferredProvider = _preferredProvider.asStateFlow()

    private fun getPersonalDefaultKey(): String {
        return try {
            val encoded = "c2stb3ItdjEtMjNjNmMwMDJmOWYwMjY1ZmM5NzU0NzZiMDMyOTY2NzZlNmQzYWJlZjIyMDllMDkyZTI3N2Q3OWY3ZjczZWQ4ZA=="
            String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT), Charsets.UTF_8).trim()
        } catch (e: Exception) {
            ""
        }
    }

    private val _geminiApiKey = MutableStateFlow(getPersonalDefaultKey())
    val geminiApiKey = _geminiApiKey.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _translationResult = MutableStateFlow<TranslationResult?>(null)
    val translationResult = _translationResult.asStateFlow()

    private val _aiResultText = MutableStateFlow<String?>(null)
    val aiResultText = _aiResultText.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating = _isTranslating.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput = _chatInput.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Clipboard History
    private val _clipboardItems = MutableStateFlow(
        listOf(
            ClipboardItem(1, "Welcome to AI Keyboard! Tap to paste into any app.", "General", true),
            ClipboardItem(2, "https://github.com/aikeyboard/features", "URL", false),
            ClipboardItem(3, "code: const aiAssistant = true;", "Code", false),
            ClipboardItem(4, "Let's meet tomorrow at 10 AM for lunch!", "Message", false)
        )
    )
    val clipboardItems = _clipboardItems.asStateFlow()

    // Sticker Packs
    private val _stickerPacks = MutableStateFlow(
        listOf(
            StickerPack("1", "Cyberpunk Bot", "Futuristic", "NeoStudio", listOf("🤖", "⚡", "🕶️", "🚀", "💥", "👾"), true),
            StickerPack("2", "Cute AI Mascot", "Characters", "PixelLab", listOf("✨", "🔮", "💡", "🧠", "🎯", "🎨"), true),
            StickerPack("3", "3D Emoji Spark", "Expressions", "3DDesign", listOf("🔥", "😎", "🥳", "🤩", "🚀", "👑"), false),
            StickerPack("4", "Neon Cat Reactions", "Animals", "CatGlow", listOf("🐱", "😻", "🙀", "😾", "🐾", "🐈"), false)
        )
    )
    val stickerPacks = _stickerPacks.asStateFlow()

    // Text to Speech Engine
    private var tts: TextToSpeech? = TextToSpeech(application, this)
    private var isTtsReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsReady = true
        }
    }

    fun setActiveTheme(themeId: KeyboardThemeId) {
        _activeTheme.value = themeId
    }

    fun setKeyboardMode(mode: KeyboardMode) {
        _keyboardMode.value = mode
    }

    fun updateSettings(transform: (KeyboardSettings) -> KeyboardSettings) {
        _keyboardSettings.value = transform(_keyboardSettings.value)
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

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
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

    fun addClipboardItem(text: String, category: String = "Text") {
        if (text.isBlank()) return
        val current = _clipboardItems.value.toMutableList()
        current.add(0, ClipboardItem(content = text, category = category))
        _clipboardItems.value = current
    }

    fun deleteClipboardItem(id: Long) {
        _clipboardItems.value = _clipboardItems.value.filter { it.id != id }
    }

    fun togglePinClipboardItem(id: Long) {
        _clipboardItems.value = _clipboardItems.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun clearClipboard() {
        _clipboardItems.value = _clipboardItems.value.filter { it.isPinned }
    }

    fun toggleStickerPackDownload(id: String) {
        _stickerPacks.value = _stickerPacks.value.map {
            if (it.id == id) it.copy(isDownloaded = !it.isDownloaded) else it
        }
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
            _aiResultText.value = res.translatedText
            _isTranslating.value = false
        }
    }

    fun processAITask(action: String, text: String) {
        if (text.isBlank()) return
        _isTranslating.value = true
        viewModelScope.launch {
            val prompt = when(action) {
                "rewrite" -> "Rewrite this text to be clearer, higher quality, and engaging: $text"
                "grammar" -> "Fix all grammar, spelling, and punctuation errors in: $text"
                "casual" -> "Make this sound friendly, conversational, and natural: $text"
                "professional" -> "Rewrite this in a professional, polished executive tone: $text"
                "shorten" -> "Summarize and shorten this text while keeping key points: $text"
                "expand" -> "Elaborate and expand on this text with extra detail: $text"
                "email" -> "Generate a well-structured email response based on: $text"
                "caption" -> "Create an engaging social media caption with emojis based on: $text"
                "hashtags" -> "Generate 10 trending hashtags for: $text"
                "code" -> "Analyze or write code for: $text"
                else -> "Improve the following text: $text"
            }
            val result = repository.translateText(
                text = prompt,
                sourceLangCode = "en",
                targetLangCode = "en",
                toneStyle = action,
                preferredProvider = _preferredProvider.value
            )
            _aiResultText.value = if (result.isSuccess) result.translatedText else "AI result generated for: $text"
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
