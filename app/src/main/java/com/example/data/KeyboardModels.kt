package com.example.data

import androidx.compose.ui.graphics.Color

enum class KeyboardThemeId(val id: String, val title: String, val category: String) {
    GLASSMORPHISM("glassmorphism", "Glassmorphism Modern", "Futuristic"),
    AMOLED_BLACK("amoled_black", "AMOLED Pitch Black", "Dark"),
    NEON_BLUE("neon_blue", "Neon Cyber Glow", "Cyberpunk"),
    CYBERPUNK("cyberpunk", "Cyberpunk 2077 Gold", "Cyberpunk"),
    IOS_STYLE("ios_style", "iOS Minimal Glass", "Clean"),
    MATERIAL_YOU("material_you", "Material You Dynamic", "Android"),
    RGB_GAMING("rgb_gaming", "RGB Gaming Chroma", "Gaming"),
    TRANSPARENT("transparent", "Frost Transparent", "Minimal"),
    GRADIENT_PURPLE("gradient_purple", "Cosmic Violet Gradient", "Gradients"),
    NATURE_FOREST("nature_forest", "Emerald Nature", "Nature"),
    ANIME_NEKO("anime_neko", "Sakura Pink Anime", "Anime")
}

data class KeyboardThemeStyle(
    val themeId: KeyboardThemeId,
    val backgroundColor: Color,
    val keyBackgroundColor: Color,
    val keyTextColor: Color,
    val accentColor: Color,
    val activeKeyColor: Color,
    val isDark: Boolean = true,
    val borderStrokeColor: Color = Color.Transparent,
    val shadowRadius: Float = 4f
)

enum class KeyboardMode {
    STANDARD,
    ONE_HAND_LEFT,
    ONE_HAND_RIGHT,
    FLOATING,
    SPLIT,
    NUMERIC_PAD,
    SYMBOL_MODE
}

data class ClipboardItem(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val category: String = "Text",
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class KeyboardSettings(
    val keyHeightDp: Int = 46,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val swipeTypingEnabled: Boolean = true,
    val autoCorrectionEnabled: Boolean = true,
    val predictionEnabled: Boolean = true,
    val showNumberRow: Boolean = true,
    val keyPopupOnTouch: Boolean = true,
    val fontSizeSp: Int = 18,
    val fontFamilyName: String = "Roboto",
    val aiOnlineMode: Boolean = true,
    val cloudSync: Boolean = true,
    val incognitoMode: Boolean = false
)

data class StickerPack(
    val id: String,
    val name: String,
    val category: String,
    val author: String,
    val stickerEmojis: List<String>,
    val isDownloaded: Boolean = false
)
