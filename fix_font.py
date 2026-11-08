import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """            ListItem(
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
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontFamily = previewFontFamily,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFontDialog = false }) {
                            Text("Tutup", color = EmeraldGreen)
                        }
                    },
                    containerColor = CardSlate
                )
            }"""

replacement = """            ListItem(
                headlineContent = { Text("Jenis Font Lirik", color = Color.White) },
                supportingContent = { Text(if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) "Custom Font" else fontLabels[lyricsFontFamily] ?: "Default", color = TextSecondary) },
                trailingContent = {
                    Box {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showFontDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) "Custom" else fontLabels[lyricsFontFamily] ?: "Default",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        if (showFontDialog) {
                            androidx.compose.ui.window.Popup(
                                alignment = Alignment.TopStart,
                                onDismissRequest = { showFontDialog = false },
                                properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .background(CardSlate, RoundedCornerShape(8.dp))
                                        .padding(3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        fontLabels.forEach { (value, label) ->
                                            val isSelected = lyricsFontFamily == value
                                            val previewFontFamily = when (value) {
                                                "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                                                "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                                                "SANS_SERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                                                "CURSIVE" -> androidx.compose.ui.text.font.FontFamily.Cursive
                                                "CUSTOM" -> customFontFamily
                                                else -> androidx.compose.ui.text.font.FontFamily.Default
                                            }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable { 
                                                        if (value == "CUSTOM") {
                                                            fontPickerLauncher.launch(arrayOf("*/*"))
                                                        } else {
                                                            viewModel.setLyricsFontFamily(value)
                                                        }
                                                        showFontDialog = false
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontFamily = previewFontFamily,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
