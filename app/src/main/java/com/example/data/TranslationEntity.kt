package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val detectedSourceLanguage: String? = null,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val tokensUsed: Int = 0,
    val modelUsed: String = "gemini-3.5-flash",
    val responseTimeMs: Long = 0
)
