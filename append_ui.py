import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

# Make sure we declare the new StateFlows at the top of the composable
decl_target = """    val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()"""
decl_replacement = """    val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()
    val lyricsContextLines by viewModel.lyricsContextLines.collectAsState()
    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()"""
content = content.replace(decl_target, decl_replacement)

# Now, add the UI for the new settings at the bottom of the scrollable column.
# Let's find the closing braces of the Column that has the ScrollState.
# Wait, the last item is the color picker for lyricsOtherFontColor.

end_target = """                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions.size) { index ->
                        val (hex, label) = colorOptions[index]
                        val isSelected = lyricsOtherFontColor == hex
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(parsedColor, RoundedCornerShape(24.dp))
                                .clickable { viewModel.setLyricsOtherFontColor(hex) }
                                .then(if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)) else Modifier)
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = if (parsedColor.luminance() > 0.5) Color.Black else Color.White, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
        }
    }
}"""

end_replacement = """                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions.size) { index ->
                        val (hex, label) = colorOptions[index]
                        val isSelected = lyricsOtherFontColor == hex
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(parsedColor, RoundedCornerShape(24.dp))
                                .clickable { viewModel.setLyricsOtherFontColor(hex) }
                                .then(if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)) else Modifier)
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = if (parsedColor.luminance() > 0.5) Color.Black else Color.White, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }

                HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))

                // Jumlah Lirik Konteks
                Text(
                    text = "Batas Baris Lirik",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                Text(
                    text = if (lyricsContextLines > 5) "Tampilkan Semua" else "Tampilkan $lyricsContextLines baris sebelum/sesudah", 
                    color = Color.White, 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Slider(
                    value = lyricsContextLines.toFloat(),
                    onValueChange = { viewModel.setLyricsContextLines(it.roundToInt()) },
                    valueRange = 1f..6f,
                    steps = 4,
                    colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen, inactiveTrackColor = CardSlate),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))

                // Efek Memudar (Fade Effect)
                Text(
                    text = "Efek Memudar",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SMOOTH" to "Halus", "SHARP" to "Tajam", "NONE" to "Mati").forEach { (value, label) ->
                        val isSelected = lyricsFadeEffect == value
                        Button(
                            onClick = { viewModel.setLyricsFadeEffect(value) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) EmeraldGreen else CardSlate,
                                contentColor = if (isSelected) Color.Black else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}"""

content = content.replace(end_target, end_replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
