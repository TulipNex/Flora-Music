import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """            Text("Pengaturan Lirik", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 82.dp)
        ) {"""

replacement = """            Text("Pengaturan Lirik", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
        }

        val align = when (lyricsAlignment) {
            "LEFT" -> TextAlign.Left
            "RIGHT" -> TextAlign.Right
            else -> TextAlign.Center
        }
        val activeColorStr = if (lyricsUseDominantColor) "#10B981" else lyricsActiveFontColor
        val otherColorStr = if (lyricsUseDominantColor) "#888888" else lyricsOtherFontColor
        val activeColor = try { Color(android.graphics.Color.parseColor(activeColorStr)) } catch(e: Exception) { Color.White }
        val otherColor = try { Color(android.graphics.Color.parseColor(otherColorStr)) } catch(e: Exception) { Color.Gray }
        val staticFontSizeOther = if (lyricsSameFontSize) lyricsActiveFontSize.sp else lyricsOtherFontSize.sp
        
        val factor = 1f / (lyricsContextLines.coerceAtLeast(1) + 1)
        val otherAlpha = if (lyricsFadeEffect == "NONE") 1.0f else if (lyricsFadeEffect == "SHARP") (1.0f - factor).coerceAtLeast(0.1f) else (1.0f - (factor * 0.5f)).coerceAtLeast(0.1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSlate.copy(alpha = 0.3f))
                .padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = lyricsMargin.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if ((lyricsActivePosition == "CENTER" || lyricsActivePosition == "BOTTOM") && lyricsContextLines > 0) {
                    Text(
                        text = "Baris lirik sebelumnya...",
                        fontSize = staticFontSizeOther,
                        color = otherColor.copy(alpha = otherAlpha),
                        textAlign = align,
                        fontWeight = FontWeight.Bold,
                        lineHeight = staticFontSizeOther * 1.5f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = "Baris lirik yang sedang diputar",
                    fontSize = lyricsActiveFontSize.sp,
                    color = activeColor,
                    textAlign = align,
                    fontWeight = FontWeight.Bold,
                    lineHeight = lyricsActiveFontSize.sp * 1.5f,
                    modifier = Modifier.fillMaxWidth()
                )
                if ((lyricsActivePosition == "CENTER" || lyricsActivePosition == "TOP") && lyricsContextLines > 0) {
                    Text(
                        text = "Baris lirik selanjutnya...",
                        fontSize = staticFontSizeOther,
                        color = otherColor.copy(alpha = otherAlpha),
                        textAlign = align,
                        fontWeight = FontWeight.Bold,
                        lineHeight = staticFontSizeOther * 1.5f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 82.dp)
        ) {"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Target not found")
