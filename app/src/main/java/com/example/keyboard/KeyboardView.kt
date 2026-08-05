package com.example.keyboard

import android.view.inputmethod.InputConnection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.Language
import com.example.data.KeyboardMode
import com.example.data.KeyboardThemeId
import com.example.repository.TranslationRepository
import com.example.ui.theme.KeyboardThemeHelper
import kotlinx.coroutines.launch

@Composable
fun KeyboardView(
    inputConnection: InputConnection?,
    repository: TranslationRepository,
    onOpenMainApp: () -> Unit,
    onVoiceInputRequest: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var activeThemeId by remember { mutableStateOf(KeyboardThemeId.GLASSMORPHISM) }
    var currentMode by remember { mutableStateOf(KeyboardMode.STANDARD) }

    var isShifted by remember { mutableStateOf(false) }
    var isNumbersMode by remember { mutableStateOf(false) }
    var isSymbolsMode by remember { mutableStateOf(false) }
    var showNumberRow by remember { mutableStateOf(true) }

    var sourceLang by remember { mutableStateOf(Language.AUTO) }
    var targetLang by remember { mutableStateOf(Language.SUPPORTED_LANGUAGES[1]) }
    var selectedTone by remember { mutableStateOf<String?>(null) }

    var isTranslating by remember { mutableStateOf(false) }
    var translatedSuggestion by remember { mutableStateOf<String?>(null) }

    val themeStyle = remember(activeThemeId) { KeyboardThemeHelper.getThemeStyle(activeThemeId) }

    val numberRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    val row1Keys = when {
        isSymbolsMode -> listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "=")
        isNumbersMode -> listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        else -> listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    }

    val row2Keys = when {
        isSymbolsMode -> listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥", "•")
        isNumbersMode -> listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
        else -> listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    }

    val row3Keys = when {
        isSymbolsMode -> listOf(".", ",", "?", "!", "'", "\"", "-")
        isNumbersMode -> listOf("*", "\"", "'", ":", ";", "!", "?")
        else -> listOf("z", "x", "c", "v", "b", "n", "m")
    }

    fun triggerTranslation() {
        val currentText = inputConnection?.getTextBeforeCursor(300, 0)?.toString() ?: ""
        if (currentText.isBlank()) return

        isTranslating = true
        scope.launch {
            val result = repository.translateText(
                text = currentText,
                sourceLangCode = sourceLang.code,
                targetLangCode = targetLang.code,
                toneStyle = selectedTone
            )
            isTranslating = false
            if (result.isSuccess && result.translatedText.isNotEmpty()) {
                translatedSuggestion = result.translatedText
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = themeStyle.backgroundColor,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            // --- Top Toolbar & Features Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Language Switcher Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeStyle.keyBackgroundColor)
                        .clickable {
                            val list = Language.SUPPORTED_LANGUAGES.filter { it.code != "auto" }
                            val currentIndex = list.indexOfFirst { it.code == targetLang.code }
                            val nextIndex = (currentIndex + 1) % list.size
                            targetLang = list[nextIndex]
                            translatedSuggestion = null
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sourceLang.flagEmoji} → ${targetLang.flagEmoji} ${targetLang.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = themeStyle.keyTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Target Language",
                        modifier = Modifier.size(16.dp),
                        tint = themeStyle.keyTextColor
                    )
                }

                // AI Action Trigger Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeStyle.accentColor)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            triggerTranslation()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Translate",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Translate",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Cycle Themes Quick Toggle
                    IconButton(
                        onClick = {
                            val allThemes = KeyboardThemeId.values()
                            val nextThemeIndex = (activeThemeId.ordinal + 1) % allThemes.size
                            activeThemeId = allThemes[nextThemeIndex]
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Change Theme", tint = themeStyle.accentColor)
                    }

                    // Open Main App Config
                    IconButton(
                        onClick = onOpenMainApp,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = "Open App", tint = themeStyle.accentColor)
                    }
                }
            }

            // --- Tone Selector Chips ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tones = listOf(
                    "Standard" to null,
                    "💼 Professional" to "professional",
                    "😊 Casual" to "casual",
                    "🙏 Polite" to "polite",
                    "✍️ Fix Grammar" to "grammar",
                    "✨ Emojis" to "emoji",
                    "📝 Summary" to "summarize"
                )

                tones.forEach { (label, toneVal) ->
                    val isSelected = selectedTone == toneVal
                    AssistChip(
                        onClick = {
                            selectedTone = toneVal
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else themeStyle.keyTextColor
                            )
                        },
                        colors = if (isSelected) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = themeStyle.accentColor,
                                labelColor = Color.White
                            )
                        } else {
                            AssistChipDefaults.assistChipColors(
                                containerColor = themeStyle.keyBackgroundColor
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // --- Translated Suggestion Banner ---
            AnimatedVisibility(
                visible = translatedSuggestion != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                translatedSuggestion?.let { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeStyle.keyBackgroundColor)
                            .border(1.dp, themeStyle.accentColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeStyle.keyTextColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(themeStyle.accentColor)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        inputConnection?.deleteSurroundingText(500, 0)
                                        inputConnection?.commitText(suggestion, 1)
                                        translatedSuggestion = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Insert", modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Insert", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(suggestion))
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                tint = themeStyle.keyTextColor
                            )
                        }
                    }
                }
            }

            // --- Dedicated Number Row ---
            if (showNumberRow && !isNumbersMode && !isSymbolsMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    numberRow.forEach { num ->
                        KeyCell(
                            text = num,
                            modifier = Modifier.weight(1f),
                            backgroundColor = themeStyle.keyBackgroundColor,
                            contentColor = themeStyle.keyTextColor
                        ) {
                            inputConnection?.commitText(num, 1)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // --- Keypad Row 1 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row1Keys.forEach { key ->
                    KeyCell(
                        text = if (isShifted) key.uppercase() else key,
                        modifier = Modifier.weight(1f),
                        backgroundColor = themeStyle.keyBackgroundColor,
                        contentColor = themeStyle.keyTextColor
                    ) {
                        val charToCommit = if (isShifted) key.uppercase() else key
                        inputConnection?.commitText(charToCommit, 1)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isShifted) isShifted = false
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Keypad Row 2 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row2Keys.forEach { key ->
                    KeyCell(
                        text = if (isShifted) key.uppercase() else key,
                        modifier = Modifier.weight(1f),
                        backgroundColor = themeStyle.keyBackgroundColor,
                        contentColor = themeStyle.keyTextColor
                    ) {
                        val charToCommit = if (isShifted) key.uppercase() else key
                        inputConnection?.commitText(charToCommit, 1)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isShifted) isShifted = false
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Keypad Row 3 (Shift, Keys, Delete) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Shift Key
                KeyCell(
                    text = if (isShifted) "⬆️" else "⇧",
                    modifier = Modifier.weight(1.3f),
                    backgroundColor = if (isShifted) themeStyle.accentColor else themeStyle.keyBackgroundColor,
                    contentColor = if (isShifted) Color.White else themeStyle.keyTextColor
                ) {
                    isShifted = !isShifted
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                row3Keys.forEach { key ->
                    KeyCell(
                        text = if (isShifted) key.uppercase() else key,
                        modifier = Modifier.weight(1f),
                        backgroundColor = themeStyle.keyBackgroundColor,
                        contentColor = themeStyle.keyTextColor
                    ) {
                        val charToCommit = if (isShifted) key.uppercase() else key
                        inputConnection?.commitText(charToCommit, 1)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isShifted) isShifted = false
                    }
                }

                // Backspace Key
                KeyCell(
                    icon = Icons.Default.Backspace,
                    modifier = Modifier.weight(1.3f),
                    backgroundColor = themeStyle.keyBackgroundColor,
                    contentColor = themeStyle.keyTextColor
                ) {
                    inputConnection?.deleteSurroundingText(1, 0)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Keypad Row 4 (Mode, Comma, Mic, Spacebar, Period, Enter) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Switcher (123 / ABC / Symbols)
                KeyCell(
                    text = when {
                        isSymbolsMode -> "ABC"
                        isNumbersMode -> "=#\\"
                        else -> "123"
                    },
                    modifier = Modifier.weight(1.2f),
                    backgroundColor = themeStyle.keyBackgroundColor,
                    contentColor = themeStyle.keyTextColor
                ) {
                    if (isSymbolsMode) {
                        isSymbolsMode = false
                        isNumbersMode = false
                    } else if (isNumbersMode) {
                        isSymbolsMode = true
                    } else {
                        isNumbersMode = true
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Comma
                KeyCell(
                    text = ",",
                    modifier = Modifier.weight(1f),
                    backgroundColor = themeStyle.keyBackgroundColor,
                    contentColor = themeStyle.keyTextColor
                ) {
                    inputConnection?.commitText(",", 1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Voice Mic
                KeyCell(
                    icon = Icons.Default.Mic,
                    modifier = Modifier.weight(1f),
                    backgroundColor = themeStyle.keyBackgroundColor,
                    contentColor = themeStyle.keyTextColor
                ) {
                    onVoiceInputRequest()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                // Interactive Spacebar with Cursor Swipe Control
                Box(
                    modifier = Modifier
                        .weight(3.5f)
                        .padding(2.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(themeStyle.keyBackgroundColor)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount > 15) {
                                    inputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                                } else if (dragAmount < -15) {
                                    inputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
                                }
                            }
                        }
                        .clickable {
                            inputConnection?.commitText(" ", 1)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${targetLang.flagEmoji} NEXUS Space",
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeStyle.keyTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Period
                KeyCell(
                    text = ".",
                    modifier = Modifier.weight(1f),
                    backgroundColor = themeStyle.keyBackgroundColor,
                    contentColor = themeStyle.keyTextColor
                ) {
                    inputConnection?.commitText(".", 1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Enter Key
                KeyCell(
                    text = "↵",
                    modifier = Modifier.weight(1.2f),
                    backgroundColor = themeStyle.accentColor,
                    contentColor = Color.White
                ) {
                    inputConnection?.commitText("\n", 1)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }
}

@Composable
fun KeyCell(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
