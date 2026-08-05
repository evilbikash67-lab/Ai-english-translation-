package com.example.api

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String,
    val isPopular: Boolean = false
) {
    companion object {
        val AUTO = Language("auto", "Auto Detect", "Auto", "🌐", true)

        val SUPPORTED_LANGUAGES = listOf(
            AUTO,
            Language("en", "English", "English", "🇺🇸", true),
            Language("hi", "Hindi", "हिन्दी", "🇮🇳", true),
            Language("es", "Spanish", "Español", "🇪🇸", true),
            Language("ur", "Urdu", "اردو", "🇵🇰", true),
            Language("fr", "French", "Français", "🇫🇷", true),
            Language("de", "German", "Deutsch", "🇩🇪", true),
            Language("ar", "Arabic", "العربية", "🇸🇦", true),
            Language("zh", "Chinese", "中文", "🇨🇳", true),
            Language("ja", "Japanese", "日本語", "🇯🇵", true),
            Language("ko", "Korean", "한국어", "🇰🇷", true),
            Language("pt", "Portuguese", "Português", "🇧🇷", true),
            Language("ru", "Russian", "Русский", "🇷🇺", true),
            Language("it", "Italian", "Italiano", "🇮🇹", true),
            Language("tr", "Turkish", "Türkçe", "🇹🇷"),
            Language("nl", "Dutch", "Nederlands", "🇳🇱"),
            Language("pl", "Polish", "Polski", "🇵🇱"),
            Language("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
            Language("vi", "Vietnamese", "Tiếng Việt", "🇻🇳"),
            Language("th", "Thai", "ไทย", "🇹🇭"),
            Language("sv", "Swedish", "Svenska", "🇸🇪"),
            Language("fa", "Persian", "فارسی", "🇮🇷"),
            Language("bn", "Bengali", "বাংলা", "🇧🇩"),
            Language("pa", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳"),
            Language("mr", "Marathi", "मराठी", "🇮🇳"),
            Language("ta", "Tamil", "தமிழ்", "🇮🇳"),
            Language("te", "Telugu", "తెలుగు", "🇮🇳"),
            Language("gu", "Gujarati", "ગુજરાતી", "🇮🇳"),
            Language("kn", "Kannada", "ಕನ್ನಡ", "🇮🇳"),
            Language("ml", "Malayalam", "മലയാളം", "🇮🇳"),
            Language("uk", "Ukrainian", "Українська", "🇺🇦"),
            Language("el", "Greek", "Ελληνικά", "🇬🇷"),
            Language("cs", "Czech", "Čeština", "🇨🇿"),
            Language("hu", "Hungarian", "Magyar", "🇭🇺"),
            Language("ro", "Romanian", "Română", "🇷🇴"),
            Language("da", "Danish", "Dansk", "🇩🇰"),
            Language("fi", "Finnish", "Suomi", "🇫🇮"),
            Language("no", "Norwegian", "Norsk", "🇳🇴"),
            Language("he", "Hebrew", "עברית", "🇮🇱"),
            Language("ms", "Malay", "Bahasa Melayu", "🇲🇾"),
            Language("fil", "Filipino", "Tagalog", "🇵🇭"),
            Language("sw", "Swahili", "Kiswahili", "🇰🇪"),
            Language("af", "Afrikaans", "Afrikaans", "🇿🇦"),
            Language("hr", "Croatian", "Hrvatski", "🇭🇷"),
            Language("sk", "Slovak", "Slovenčina", "🇸🇰"),
            Language("bg", "Bulgarian", "Български", "🇧🇬"),
            Language("lt", "Lithuanian", "Lietuvių", "🇱🇹"),
            Language("sr", "Serbian", "Српски", "🇷🇸"),
            Language("sq", "Albanian", "Shqip", "🇦🇱"),
            Language("ka", "Georgian", "ქართული", "🇬🇪")
        )

        fun findByCode(code: String): Language {
            return SUPPORTED_LANGUAGES.find { it.code.equals(code, ignoreCase = true) } ?: AUTO
        }
    }
}
