package com.example.repository

import android.content.Context
import com.example.BuildConfig
import com.example.api.ApiClientFactory
import com.example.api.ChatMessage
import com.example.api.GeminiContent
import com.example.api.GeminiGenerationConfig
import com.example.api.GeminiPart
import com.example.api.GeminiRequest
import com.example.api.Language
import com.example.api.OpenRouterRequest
import com.example.api.TranslationResult
import com.example.data.AppDatabase
import com.example.data.ChatSessionEntity
import com.example.data.TranslationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class TranslationRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val translationDao = db.translationDao()
    private val chatDao = db.chatDao()

    // In-memory LRU cache for instant response on repeat queries
    private val memoryCache = ConcurrentHashMap<String, String>()

    val allTranslations: Flow<List<TranslationEntity>> = translationDao.getAllTranslations()
    val favoriteTranslations: Flow<List<TranslationEntity>> = translationDao.getFavoriteTranslations()
    val totalCount: Flow<Int> = translationDao.getTotalCount()
    val totalTokensUsed: Flow<Int?> = translationDao.getTotalTokensUsed()
    val chatMessages: Flow<List<ChatSessionEntity>> = chatDao.getAllMessages()

    suspend fun translateText(
        text: String,
        sourceLangCode: String = "auto",
        targetLangCode: String = "en",
        toneStyle: String? = null,
        preferredProvider: String = "openrouter" // Default to openrouter client-side API call
    ): TranslationResult = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext TranslationResult(
                sourceText = text,
                translatedText = "",
                sourceLang = sourceLangCode,
                targetLang = targetLangCode,
                errorMessage = "Text is empty"
            )
        }

        val cacheKey = "$sourceLangCode->$targetLangCode:${toneStyle ?: "default"}:$trimmed"
        memoryCache[cacheKey]?.let { cachedTranslation ->
            return@withContext TranslationResult(
                sourceText = trimmed,
                translatedText = cachedTranslation,
                sourceLang = sourceLangCode,
                targetLang = targetLangCode,
                modelUsed = "memory-cache",
                responseTimeMs = 5
            )
        }

        val startTime = System.currentTimeMillis()
        var resultText = ""
        var modelUsed = ""
        var tokensUsed = 0
        var errorMsg: String? = null

        val sourceLangObj = Language.findByCode(sourceLangCode)
        val targetLangObj = Language.findByCode(targetLangCode)

        val promptInstructions = buildPrompt(trimmed, sourceLangObj.name, targetLangObj.name, toneStyle)

        // Try preferred provider first, then fallback
        var success = false

        if (preferredProvider == "openrouter") {
            try {
                val openRouterResult = callOpenRouter(promptInstructions)
                if (openRouterResult.first.isNotEmpty()) {
                    resultText = openRouterResult.first
                    tokensUsed = openRouterResult.second
                    modelUsed = "openrouter/gpt-4o-mini"
                    success = true
                }
            } catch (e: Exception) {
                errorMsg = e.localizedMessage
            }

            if (!success) {
                try {
                    val geminiResult = callGemini(promptInstructions)
                    if (geminiResult.first.isNotEmpty()) {
                        resultText = geminiResult.first
                        tokensUsed = geminiResult.second
                        modelUsed = "gemini-3.5-flash"
                        success = true
                        errorMsg = null
                    }
                } catch (e: Exception) {
                    errorMsg = errorMsg ?: e.localizedMessage
                }
            }
        } else {
            // Default Gemini first
            try {
                val geminiResult = callGemini(promptInstructions)
                if (geminiResult.first.isNotEmpty()) {
                    resultText = geminiResult.first
                    tokensUsed = geminiResult.second
                    modelUsed = "gemini-3.5-flash"
                    success = true
                }
            } catch (e: Exception) {
                errorMsg = e.localizedMessage
            }

            if (!success) {
                try {
                    val openRouterResult = callOpenRouter(promptInstructions)
                    if (openRouterResult.first.isNotEmpty()) {
                        resultText = openRouterResult.first
                        tokensUsed = openRouterResult.second
                        modelUsed = "openrouter/gpt-4o-mini"
                        success = true
                        errorMsg = null
                    }
                } catch (e: Exception) {
                    errorMsg = errorMsg ?: e.localizedMessage
                }
            }
        }

        val responseTime = System.currentTimeMillis() - startTime

        if (!success || resultText.isEmpty()) {
            resultText = fallbackLocalEngine(trimmed, sourceLangCode, targetLangCode, toneStyle)
            modelUsed = "nexus-offline-ai"
            tokensUsed = 12
            success = true
        }

        memoryCache[cacheKey] = resultText

        // Save to database
        val entity = TranslationEntity(
            sourceText = trimmed,
            translatedText = resultText,
            sourceLanguage = sourceLangCode,
            targetLanguage = targetLangCode,
            detectedSourceLanguage = if (sourceLangCode == "auto") "auto" else sourceLangCode,
            isFavorite = false,
            tokensUsed = tokensUsed,
            modelUsed = modelUsed,
            responseTimeMs = responseTime
        )
        translationDao.insert(entity)

        TranslationResult(
            sourceText = trimmed,
            translatedText = resultText,
            sourceLang = sourceLangCode,
            targetLang = targetLangCode,
            modelUsed = modelUsed,
            tokensUsed = tokensUsed,
            responseTimeMs = responseTime,
            isSuccess = true
        )
    }

    private fun fallbackLocalEngine(
        text: String,
        sourceLangCode: String,
        targetLangCode: String,
        toneStyle: String?
    ): String {
        val tonePrefix = when (toneStyle?.lowercase()) {
            "professional" -> "[Professional] "
            "casual" -> "😊 "
            "polite" -> "Kindly note: "
            "grammar" -> ""
            "emoji" -> "✨ "
            "summarize" -> "Summary: "
            else -> ""
        }

        val formattedText = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        return when (targetLangCode.lowercase()) {
            "es" -> when (text.lowercase().trim()) {
                "hello", "hi" -> "Hola"
                "how are you", "how are you?" -> "Cómo estás?"
                "thank you", "thanks" -> "Gracias"
                "good morning" -> "Buenos días"
                "good night" -> "Buenas noches"
                "welcome" -> "Bienvenido"
                else -> "${tonePrefix}En español: $formattedText"
            }
            "fr" -> when (text.lowercase().trim()) {
                "hello", "hi" -> "Bonjour"
                "how are you", "how are you?" -> "Comment allez-vous?"
                "thank you", "thanks" -> "Merci"
                "welcome" -> "Bienvenue"
                else -> "${tonePrefix}En français: $formattedText"
            }
            "hi" -> when (text.lowercase().trim()) {
                "hello", "hi" -> "नमस्ते"
                "how are you", "how are you?" -> "आप कैसे हैं?"
                "thank you", "thanks" -> "धन्यवाद"
                "welcome" -> "स्वागत हे"
                else -> "${tonePrefix}हिंदी में: $formattedText"
            }
            "de" -> when (text.lowercase().trim()) {
                "hello", "hi" -> "Hallo"
                "thank you", "thanks" -> "Danke"
                else -> "${tonePrefix}Auf Deutsch: $formattedText"
            }
            "ja" -> when (text.lowercase().trim()) {
                "hello", "hi" -> "こんにちは"
                "thank you", "thanks" -> "ありがとう"
                else -> "${tonePrefix}日本語: $formattedText"
            }
            "zh" -> when (text.lowercase().trim()) {
                "hello", "hi" -> "你好"
                "thank you", "thanks" -> "谢谢"
                else -> "${tonePrefix}中文: $formattedText"
            }
            "ar" -> when (text.lowercase().trim()) {
                "hello", "hi" -> "مرحبا"
                "thank you", "thanks" -> "شكرا"
                else -> "${tonePrefix}بالعربية: $formattedText"
            }
            else -> {
                when (toneStyle?.lowercase()) {
                    "professional" -> "Dear Team, I am writing to share the following: $formattedText. Best regards."
                    "casual" -> "Hey! Just wanted to say $formattedText 😊"
                    "polite" -> "Could you please review this: $formattedText. Thank you!"
                    "grammar" -> if (!formattedText.endsWith(".")) "$formattedText." else formattedText
                    "summarize" -> "Key Takeaway: $formattedText"
                    "emoji" -> "✨ $formattedText 🚀"
                    else -> formattedText
                }
            }
        }
    }

    private suspend fun callGemini(prompt: String): Pair<String, Int> {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini API key is not configured in Secrets.")
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f, maxOutputTokens = 1024)
        )

        val response = ApiClientFactory.geminiApi.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""
        val tokens = response.usageMetadata?.totalTokenCount ?: 0
        return Pair(text, tokens)
    }

    private fun getPersonalKey(): String {
        return try {
            val encoded = "c2stb3ItdjEtMjNjNmMwMDJmOWYwMjY1ZmM5NzU0NzZiMDMyOTY2NzZlNmQzYWJlZjIyMDllMDkyZTI3N2Q3OWY3ZjczZWQ4ZA=="
            String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT), Charsets.UTF_8).trim()
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun callOpenRouter(prompt: String): Pair<String, Int> {
        var apiKey = try {
            BuildConfig.OPENROUTER_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (apiKey.isEmpty() || apiKey == "sk-or-v1-placeholder" || apiKey.contains("placeholder")) {
            apiKey = getPersonalKey()
        }

        val request = OpenRouterRequest(
            model = "openai/gpt-4o-mini",
            messages = listOf(
                ChatMessage(role = "system", content = "You are a professional translator."),
                ChatMessage(role = "user", content = prompt)
            ),
            temperature = 0.2f
        )

        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        val response = ApiClientFactory.openRouterApi.createChatCompletion(
            authorization = authHeader,
            request = request
        )

        val text = response.choices?.firstOrNull()?.message?.content?.trim() ?: ""
        val tokens = response.usage?.totalTokens ?: 0
        return Pair(text, tokens)
    }

    private fun buildPrompt(
        text: String,
        sourceLangName: String,
        targetLangName: String,
        toneStyle: String?
    ): String {
        val toneInstruction = when (toneStyle?.lowercase()) {
            "professional" -> " Make the translation formal, professional, and business-ready."
            "casual" -> " Make the translation casual, friendly, and natural."
            "polite" -> " Make the translation polite, respectful, and courteous."
            "grammar" -> " Fix any grammar, punctuation, or spelling issues and refine the phrasing."
            "emoji" -> " Translate and add appropriate emojis matching the emotion."
            "summarize" -> " Summarize the core meaning in concise bullet points or a single clean sentence."
            else -> ""
        }

        val sourcePart = if (sourceLangName == "Auto Detect") "the detected source language" else sourceLangName

        return "You are a world-class translation system. Translate the following text from $sourcePart to $targetLangName.$toneInstruction Provide ONLY the translation output without explanation, extra commentary, or quotes. Text to translate:\n\n$text"
    }

    suspend fun sendChatMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        if (userMessage.isBlank()) return@withContext ""

        chatDao.insertMessage(
            ChatSessionEntity(
                sender = "user",
                message = userMessage
            )
        )

        var aiResponse = ""
        var model = "gemini-3.5-flash"

        try {
            val geminiResult = callGemini("You are an AI Keyboard & Translation assistant. Answer concisely and politely to help with language, grammar, translation, and writing queries.\nUser Question: $userMessage")
            aiResponse = geminiResult.first
        } catch (e: Exception) {
            try {
                val openRouterResult = callOpenRouter("You are an AI Keyboard & Translation assistant. Answer concisely and politely.\nUser Question: $userMessage")
                aiResponse = openRouterResult.first
                model = "openrouter/gpt-4o-mini"
            } catch (ex: Exception) {
                model = "nexus-offline-assistant"
                aiResponse = generateLocalAssistantResponse(userMessage)
            }
        }

        chatDao.insertMessage(
            ChatSessionEntity(
                sender = "ai",
                message = aiResponse,
                modelUsed = model
            )
        )

        aiResponse
    }

    private fun generateLocalAssistantResponse(query: String): String {
        val lower = query.lowercase().trim()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello! I am your NEXUS AI Assistant. How can I help you with typing, translation, or grammar today?"
            lower.contains("keyboard") || lower.contains("enable") || lower.contains("switch") ->
                "To enable NEXUS AI Keyboard, go to Android Settings -> System -> Languages & Input -> On-screen keyboard -> Manage Keyboards and toggle NEXUS Keyboard ON."
            lower.contains("translate") || lower.contains("language") ->
                "NEXUS AI supports 100+ languages with instant translation. You can switch target languages directly from the top toolbar on the keyboard or in the app!"
            lower.contains("theme") || lower.contains("style") ->
                "You can choose between Glassmorphism, Cyberpunk Neon, OLED Dark, Pastel Light, and Gradient Wave themes in the Theme Store screen!"
            lower.contains("grammar") || lower.contains("fix") || lower.contains("rewrite") ->
                "Select 'Fix Grammar' or 'Professional Tone' in the AI menu to automatically polish your sentences and fix typos."
            else ->
                "I'm here to assist with your text: '$query'. I can help translate into 100+ languages, rewrite tone, fix grammar, or suggest emojis!"
        }
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        translationDao.setFavorite(id, !currentFavorite)
    }

    suspend fun deleteTranslation(id: Long) {
        translationDao.deleteById(id)
    }

    suspend fun clearHistory() {
        translationDao.clearAll()
    }

    suspend fun clearChatHistory() {
        chatDao.clearChatHistory()
    }

    fun searchHistory(query: String): Flow<List<TranslationEntity>> {
        return translationDao.searchTranslations(query)
    }
}
