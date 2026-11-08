import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

decl_target = """    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()"""
decl_replacement = """    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()
    val lyricsMargin by viewModel.lyricsMargin.collectAsState()"""
content = content.replace(decl_target, decl_replacement)

ui_target = """                // Efek Memudar (Fade Effect)"""
ui_replacement = """                // Margin Horizontal
                Text(
                    text = "Margin Horizontal (Jarak Tepi)",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                
                Text(
                    text = "${lyricsMargin.roundToInt()}dp", 
                    color = Color.White, 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Slider(
                    value = lyricsMargin,
                    onValueChange = { viewModel.setLyricsMargin(it) },
                    valueRange = 0f..64f,
                    steps = 15,
                    colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen, inactiveTrackColor = CardSlate),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))

                // Efek Memudar (Fade Effect)"""
content = content.replace(ui_target, ui_replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
