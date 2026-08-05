package com.example.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://aitranslatekeyboard.com",
        @Header("X-Title") appTitle: String = "AI Translate Keyboard",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}
