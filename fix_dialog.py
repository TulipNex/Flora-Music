import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """            // ==========================================
            // SECTION 2: TIPOGRAFI
            // ==========================================
            SettingsSectionTitle("Tipografi")"""

replacement = """            // ==========================================
            // SECTION 1.5: FONT LIRIK
            // ==========================================
            SettingsSectionTitle("Font Lirik")
            
            val fontLabels = mapOf(
                "DEFAULT" to "Default (Sistem)",
                "SERIF" to "Serif",
                "SANS_SERIF" to "Sans Serif",
                "MONOSPACE" to "Monospace",
                "CURSIVE" to "Cursive",
                "CUSTOM" to "Custom (Impor)"
            )
            
            ListItem(
                headlineContent = { Text("Jenis Font Lirik", color = Color.White) },
                supportingContent = { Text(if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) "Custom Font" else fontLabels[lyricsFontFamily] ?: "Default", color = TextSecondary) },
                trailingContent = {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Pilih", tint = Color.White)
                },
                modifier = Modifier.clickable { showFontDialog = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            if (showFontDialog) {
                AlertDialog(
                    onDismissRequest = { showFontDialog = false },
                    title = { Text("Pilih Jenis Font", color = Color.White) },
                    text = {
                        Column {
                            fontLabels.forEach { (value, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (value == "CUSTOM") {
                                                fontPickerLauncher.launch(arrayOf("*/*"))
                                            } else {
                                                viewModel.setLyricsFontFamily(value)
                                            }
                                            showFontDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val previewFontFamily = when (value) {
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
                                    Text(label, color = Color.White, fontFamily = previewFontFamily)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFontDialog = false }) {
                            Text("Batal", color = EmeraldGreen)
                        }
                    },
                    containerColor = CardSlate
                )
            }

            // ==========================================
            // SECTION 2: TIPOGRAFI
            // ==========================================
            SettingsSectionTitle("Tipografi")"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
