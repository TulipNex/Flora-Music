import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

old_lh1 = """                                                lyricStyle = TextStyle(
                                                    fontFamily = customFontFamily,
                                                    fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = align,
                                                    lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp
                                                )"""
new_lh1 = """                                                lyricStyle = TextStyle(
                                                    fontFamily = customFontFamily,
                                                    fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = align,
                                                    lineHeight = if (line.isBackground) (baseFontSize * 0.75f * lyricsLineHeightMultiplier).sp else (baseFontSize * lyricsLineHeightMultiplier).sp
                                                )"""
content = content.replace(old_lh1, new_lh1)

old_lh2 = """                                            Text(
                                                text = displayText,
                                                fontFamily = customFontFamily,
                                                fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorState,
                                                textAlign = align,
                                                lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )"""
new_lh2 = """                                            Text(
                                                text = displayText,
                                                fontFamily = customFontFamily,
                                                fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorState,
                                                textAlign = align,
                                                lineHeight = if (line.isBackground) (baseFontSize * 0.75f * lyricsLineHeightMultiplier).sp else (baseFontSize * lyricsLineHeightMultiplier).sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )"""
content = content.replace(old_lh2, new_lh2)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
