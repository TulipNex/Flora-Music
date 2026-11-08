import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target1 = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()"""
replacement1 = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()
    val lyricsFontFamily by viewModel.lyricsFontFamily.collectAsState()
    val lyricsCustomFontUri by viewModel.lyricsCustomFontUri.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val customFontFamily = remember(lyricsFontFamily, lyricsCustomFontUri) {
        if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(lyricsCustomFontUri)
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val typeface = android.graphics.Typeface.Builder(pfd.fileDescriptor).build()
                    pfd.close()
                    androidx.compose.ui.text.font.FontFamily(typeface)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } ?: androidx.compose.ui.text.font.FontFamily.Default
        } else {
            when (lyricsFontFamily) {
                "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                "SANS_SERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                "CURSIVE" -> androidx.compose.ui.text.font.FontFamily.Cursive
                else -> androidx.compose.ui.text.font.FontFamily.Default
            }
        }
    }"""
content = content.replace(target1, replacement1)

target2 = """                                    fontSize = 18.sp,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 32.sp,"""
replacement2 = """                                    fontFamily = customFontFamily,
                                    fontSize = 18.sp,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 32.sp,"""
content = content.replace(target2, replacement2)

target3 = """                                        text = line.text,
                                        fontSize = baseFontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = (baseFontSize * 1.5f).sp,"""
replacement3 = """                                        text = line.text,
                                        fontFamily = customFontFamily,
                                        fontSize = baseFontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = (baseFontSize * 1.5f).sp,"""
content = content.replace(target3, replacement3)

target4 = """                        fontSize = 16.sp,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp,"""
replacement4 = """                        fontFamily = customFontFamily,
                        fontSize = 16.sp,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp,"""
content = content.replace(target4, replacement4)

target5 = """                        text = line.text,
                        fontSize = baseFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorState,
                        textAlign = align,
                        lineHeight = (baseFontSize * 1.5f).sp,"""
replacement5 = """                        text = line.text,
                        fontFamily = customFontFamily,
                        fontSize = baseFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorState,
                        textAlign = align,
                        lineHeight = (baseFontSize * 1.5f).sp,"""
content = content.replace(target5, replacement5)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

