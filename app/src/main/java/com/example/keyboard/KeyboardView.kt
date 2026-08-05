package com.example.keyboard

import android.view.inputmethod.InputConnection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.Language
import com.example.repository.TranslationRepository
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

    var isShifted by remember { mutableStateOf(false) }
    var isNumbersMode by remember { mutableStateOf(false) }
    var sourceLang by remember { mutableStateOf(Language.AUTO) }
    var targetLang by remember { mutableStateOf(Language.SUPPORTED_LANGUAGES[1]) } // Default EN or HI
    var selectedTone by remember { mutableStateOf<String?>(null) }

    var isTranslating by remember { mutableStateOf(false) }
    var translatedSuggestion by remember { mutableStateOf<String?>(null) }

    val row1Keys = if (isNumbersMode) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    else listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")

    val row2Keys = if (isNumbersMode) listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
    else listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")

    val row3Keys = if (isNumbersMode) listOf("*", "\"", "'", ":", ";", "!", "?")
    else listOf("z", "x", "c", "v", "b", "n", "m")

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
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            // --- Top Toolbar & Language Switcher ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Language Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            // Cycle through target languages
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Target Language",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // AI Translate Action Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
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
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Translate",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Translate",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Main App Launcher Icon
                IconButton(
                    onClick = onOpenMainApp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Open App",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- Tone Selector Horizontal Strip ---
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = if (isSelected) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            AssistChipDefaults.assistChipColors()
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // --- Real-time Translation Candidate Suggestion Bar ---
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
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row {
                            // Insert Suggestion
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        // Replace current line with translated text
                                        inputConnection?.deleteSurroundingText(500, 0)
                                        inputConnection?.commitText(suggestion, 1)
                                        translatedSuggestion = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Insert Text",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onTertiary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Insert",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // Copy Suggestion
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Translation",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(suggestion))
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // --- Keypad Row 1 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row1Keys.forEach { key ->
                    KeyCell(
                        text = if (isShifted) key.uppercase() else key,
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
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
                    backgroundColor = if (isShifted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                ) {
                    isShifted = !isShifted
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                row3Keys.forEach { key ->
                    KeyCell(
                        text = if (isShifted) key.uppercase() else key,
                        modifier = Modifier.weight(1f)
                    ) {
                        val charToCommit = if (isShifted) key.uppercase() else key
                        inputConnection?.commitText(charToCommit, 1)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isShifted) isShifted = false
                    }
                }

                // Delete Key
                KeyCell(
                    icon = Icons.Default.Backspace,
                    modifier = Modifier.weight(1.3f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    inputConnection?.deleteSurroundingText(1, 0)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Keypad Row 4 (123, Comma, Mic, Spacebar, Period, Enter) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Toggle (123 / ABC)
                KeyCell(
                    text = if (isNumbersMode) "ABC" else "123",
                    modifier = Modifier.weight(1.2f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    isNumbersMode = !isNumbersMode
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Comma
                KeyCell(
                    text = ",",
                    modifier = Modifier.weight(1f)
                ) {
                    inputConnection?.commitText(",", 1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Mic Voice Input
                KeyCell(
                    icon = Icons.Default.Mic,
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    onVoiceInputRequest()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                // Spacebar
                KeyCell(
                    text = "${targetLang.flagEmoji} Space",
                    modifier = Modifier.weight(3.5f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    inputConnection?.commitText(" ", 1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Period
                KeyCell(
                    text = ".",
                    modifier = Modifier.weight(1f)
                ) {
                    inputConnection?.commitText(".", 1)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Enter Key
                KeyCell(
                    text = "↵",
                    modifier = Modifier.weight(1.2f),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
