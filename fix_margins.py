import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target_1 = """                                val baseFontSize = if (lyricsSameFontSize) lyricsActiveFontSize else lyricsOtherFontSize
                                val maxFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize
                                val targetScale = if (distance == 0) (maxFontSize / baseFontSize) else 1f"""

replacement_1 = """                                val baseFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize.toFloat()
                                val targetScale = if (distance == 0) 1f else {
                                    if (lyricsSameFontSize) (lyricsActiveFontSize / baseFontSize) else (lyricsOtherFontSize / baseFontSize)
                                }"""

content = content.replace(target_1, replacement_1)

target_2 = """                    val baseFontSize = if (lyricsSameFontSize) lyricsActiveFontSize else lyricsOtherFontSize
                    val maxFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize
                    val targetScale = if (distance == 0) (maxFontSize / baseFontSize) else 1f"""

replacement_2 = """                    val baseFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize.toFloat()
                    val targetScale = if (distance == 0) 1f else {
                        if (lyricsSameFontSize) (lyricsActiveFontSize / baseFontSize) else (lyricsOtherFontSize / baseFontSize)
                    }"""

content = content.replace(target_2, replacement_2)

target_3 = """                                        fontSize = baseFontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = (baseFontSize * 1.5f).sp,"""

replacement_3 = """                                        fontSize = baseFontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = (baseFontSize * 1.5f).sp,"""

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

