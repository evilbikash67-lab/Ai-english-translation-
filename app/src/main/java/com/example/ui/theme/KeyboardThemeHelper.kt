package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.KeyboardThemeId
import com.example.data.KeyboardThemeStyle

object KeyboardThemeHelper {

    fun getThemeStyle(themeId: KeyboardThemeId): KeyboardThemeStyle {
        return when (themeId) {
            KeyboardThemeId.GLASSMORPHISM -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xDD12131C),
                keyBackgroundColor = Color(0x33FFFFFF),
                keyTextColor = Color(0xFFE2E8F0),
                accentColor = Color(0xFF6366F1),
                activeKeyColor = Color(0x666366F1),
                isDark = true,
                borderStrokeColor = Color(0x33FFFFFF),
                shadowRadius = 8f
            )
            KeyboardThemeId.AMOLED_BLACK -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFF000000),
                keyBackgroundColor = Color(0xFF121212),
                keyTextColor = Color(0xFFFFFFFF),
                accentColor = Color(0xFF00E5FF),
                activeKeyColor = Color(0xFF1E293B),
                isDark = true,
                borderStrokeColor = Color(0xFF262626)
            )
            KeyboardThemeId.NEON_BLUE -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFF090D16),
                keyBackgroundColor = Color(0xFF131C2E),
                keyTextColor = Color(0xFF38BDF8),
                accentColor = Color(0xFF0EA5E9),
                activeKeyColor = Color(0xFF0284C7),
                isDark = true,
                borderStrokeColor = Color(0x6638BDF8)
            )
            KeyboardThemeId.CYBERPUNK -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFF180A29),
                keyBackgroundColor = Color(0xFF2B1047),
                keyTextColor = Color(0xFFFACC15),
                accentColor = Color(0xFFEC4899),
                activeKeyColor = Color(0xFF831843),
                isDark = true,
                borderStrokeColor = Color(0x88EC4899)
            )
            KeyboardThemeId.IOS_STYLE -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFFD1D5DB),
                keyBackgroundColor = Color(0xFFFFFFFF),
                keyTextColor = Color(0xFF111827),
                accentColor = Color(0xFF2563EB),
                activeKeyColor = Color(0xFFE5E7EB),
                isDark = false,
                borderStrokeColor = Color(0x449CA3AF)
            )
            KeyboardThemeId.MATERIAL_YOU -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFF1F1B24),
                keyBackgroundColor = Color(0xFF332D3B),
                keyTextColor = Color(0xFFE8DEF8),
                accentColor = Color(0xFFD0BCFF),
                activeKeyColor = Color(0xFF4F378B),
                isDark = true,
                borderStrokeColor = Color(0x33D0BCFF)
            )
            KeyboardThemeId.RGB_GAMING -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFF050508),
                keyBackgroundColor = Color(0xFF12121D),
                keyTextColor = Color(0xFF22D3EE),
                accentColor = Color(0xFFA855F7),
                activeKeyColor = Color(0xFF2563EB),
                isDark = true,
                borderStrokeColor = Color(0xAA10B981)
            )
            KeyboardThemeId.TRANSPARENT -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0x660F172A),
                keyBackgroundColor = Color(0x22FFFFFF),
                keyTextColor = Color(0xFFF8FAFC),
                accentColor = Color(0xFF38BDF8),
                activeKeyColor = Color(0x4438BDF8),
                isDark = true,
                borderStrokeColor = Color(0x22FFFFFF)
            )
            KeyboardThemeId.GRADIENT_PURPLE -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFF2E1065),
                keyBackgroundColor = Color(0xFF4C1D95),
                keyTextColor = Color(0xFFF3E8FF),
                accentColor = Color(0xFFC084FC),
                activeKeyColor = Color(0xFF6B21A8),
                isDark = true,
                borderStrokeColor = Color(0x55E9D5FF)
            )
            KeyboardThemeId.NATURE_FOREST -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFF062C1E),
                keyBackgroundColor = Color(0xFF0F4C36),
                keyTextColor = Color(0xFFD1FAE5),
                accentColor = Color(0xFF10B981),
                activeKeyColor = Color(0xFF047857),
                isDark = true,
                borderStrokeColor = Color(0x4434D399)
            )
            KeyboardThemeId.ANIME_NEKO -> KeyboardThemeStyle(
                themeId = themeId,
                backgroundColor = Color(0xFF500724),
                keyBackgroundColor = Color(0xFF831843),
                keyTextColor = Color(0xFFFCE7F3),
                accentColor = Color(0xFFF472B6),
                activeKeyColor = Color(0xFF9D174D),
                isDark = true,
                borderStrokeColor = Color(0x66F472B6)
            )
        }
    }
}
