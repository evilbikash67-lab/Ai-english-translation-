# AI Translate Keyboard 🌐✨

An AI-powered Android Keyboard application with real-time translation, voice speech input, clipboard translation, local history, and AI chat assistant. Powered by Google Gemini and OpenRouter APIs.

---

## 📱 Features

- **🔄 Real-Time Translation Keyboard**: Type in your native language in any app, and translate instantly above the keyboard or directly into text fields.
- **🗣️ Voice Input & Text-To-Speech**: Speak in any language using microphone access and listen to translations in natural synthesized voice audio.
- **📋 Clipboard Auto-Translate**: Automatically detects copied text from clipboard and offers a 1-tap translation chip.
- **🌐 50+ Languages Supported**: Instant switching between popular languages (English, Hindi, Spanish, French, German, Urdu, Arabic, Chinese, Japanese, Korean, etc.) with flag icons.
- **✍️ Tone Transformer**: Translate text with specific tones: *Professional, Casual, Polite, Fix Grammar, Emojis, or Summary*.
- **📜 Searchable History**: Stores all translations locally using Room Database with search, favorite starring, copy, share, and delete options.
- **💬 AI Chat Assistant**: Built-in language assistant powered by Gemini / OpenRouter to rephrase, fix grammar, or answer questions.
- **⚙️ Customizable Settings**: Select preferred AI model provider (Gemini 3.5 Flash or OpenRouter GPT-4o-mini).

---

## 🛠️ Architecture & Tech Stack

### Android App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Keyboard Service**: Android `InputMethodService` + Compose UI
- **Database**: Room Database + KSP (Kotlin Symbol Processing)
- **Networking**: Retrofit 2 + Moshi + OkHttp
- **Architecture**: MVVM + Repository Pattern + Coroutines/Flow

### Secrets & API Keys
Secrets like `GEMINI_API_KEY` and `OPENROUTER_API_KEY` are configured via the **Secrets Panel in AI Studio** or defined in `.env` / `.env.example` and accessed via `BuildConfig`.

### 🔑 Release Build & Signing
To build a signed release APK or Bundle (`./gradlew assembleRelease`), ensure you provide a keystore file and set the following environment variables:
- `KEYSTORE_PATH`: Path to your keystore `.jks` file (defaults to `${rootDir}/my-upload-key.jks`).
- `STORE_PASSWORD`: Keystore password.
- `KEY_PASSWORD`: Alias key password.

---

## 🚀 Quick Start Guide

1. **Build & Run App**:
   Select the project in AI Studio or Android Studio and click **Run**.

2. **Enable AI Keyboard on Device**:
   - Open **AI Translate Keyboard** app.
   - On the Home screen card, tap **"Enable / Switch Keyboard in Settings"**.
   - Enable **AI Translate Keyboard** in Android System Input Method settings.
   - Select **AI Translate Keyboard** as active input method.

3. **Use in Any App**:
   - Open WhatsApp, Messages, or any text editor.
   - Start typing text, tap **AI Translate**, or select tone style to insert translated text instantly!

---

## 📜 License
MIT License
