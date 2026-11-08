import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()"""
replacement1 = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()
    val lyricsItemSpacing by viewModel.lyricsItemSpacing.collectAsState()
    val lyricsLineHeightMultiplier by viewModel.lyricsLineHeightMultiplier.collectAsState()"""

content = content.replace(target1, replacement1)

target2 = """            // Margin Horizontal
            Text(
                text = "Margin Horizontal: ${lyricsMargin.roundToInt()}dp","""
replacement2 = """            // Jarak Antar Baris (Item Spacing)
            Text(
                text = "Jarak Antar Baris Lirik: ${lyricsItemSpacing.roundToInt()}dp", 
                color = Color.White, 
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Slider(
                value = lyricsItemSpacing,
                onValueChange = { viewModel.setLyricsItemSpacing(it) },
                valueRange = 8f..64f,
                steps = 13,
                colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen, inactiveTrackColor = CardSlate),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Margin Horizontal
            Text(
                text = "Margin Horizontal: ${lyricsMargin.roundToInt()}dp","""

content = content.replace(target2, replacement2)

target3 = """            SettingsSectionTitle("Tipografi")
            
            // Ukuran Font Sama"""
replacement3 = """            SettingsSectionTitle("Tipografi")
            
            // Jarak Antar Kata (Line Height)
            val formattedLineHeight = String.format(java.util.Locale.US, "%.1f", lyricsLineHeightMultiplier)
            Text(
                text = "Jarak Baris (Spasi): ${formattedLineHeight}x", 
                color = Color.White, 
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Slider(
                value = lyricsLineHeightMultiplier,
                onValueChange = { viewModel.setLyricsLineHeightMultiplier(it) },
                valueRange = 1.0f..2.5f,
                steps = 14,
                colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen, inactiveTrackColor = CardSlate),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Ukuran Font Sama"""

content = content.replace(target3, replacement3)

# Update the preview in the settings screen
target_preview1 = """                        lineHeight = staticFontSizeOther * 1.5f,"""
replacement_preview1 = """                        lineHeight = (staticFontSizeOther * lyricsLineHeightMultiplier).sp,"""
content = content.replace(target_preview1, replacement_preview1)

target_preview2 = """                    lineHeight = lyricsActiveFontSize.sp * 1.5f,"""
replacement_preview2 = """                    lineHeight = (lyricsActiveFontSize * lyricsLineHeightMultiplier).sp,"""
content = content.replace(target_preview2, replacement_preview2)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
