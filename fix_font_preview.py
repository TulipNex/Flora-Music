import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """                                    RadioButton(
                                        selected = lyricsFontFamily == value,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen, unselectedColor = Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, color = Color.White)"""
                                    
replacement = """                                    val previewFontFamily = when (value) {
                                        "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                                        "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                                        "SANS_SERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                                        "CURSIVE" -> androidx.compose.ui.text.font.FontFamily.Cursive
                                        "CUSTOM" -> customFontFamily
                                        else -> androidx.compose.ui.text.font.FontFamily.Default
                                    }
                                    RadioButton(
                                        selected = lyricsFontFamily == value,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen, unselectedColor = Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, color = Color.White, fontFamily = previewFontFamily)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
