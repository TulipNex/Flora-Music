import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """    var showFontDialog by remember { mutableStateOf(false) }"""
replacement1 = """    var showFontDialog by remember { mutableStateOf(false) }
    
    val customFontFamily = remember(lyricsFontFamily, lyricsCustomFontUri) {
        if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(lyricsCustomFontUri)
                val context = androidx.compose.ui.platform.LocalContext.current
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

target2 = """                        text = "Baris lirik sebelumnya...",
                        fontSize = staticFontSizeOther,
                        color = otherColor.copy(alpha = otherAlpha),"""
replacement2 = """                        text = "Baris lirik sebelumnya...",
                        fontFamily = customFontFamily,
                        fontSize = staticFontSizeOther,
                        color = otherColor.copy(alpha = otherAlpha),"""
content = content.replace(target2, replacement2)

target3 = """                    text = "Baris lirik yang sedang diputar",
                    fontSize = lyricsActiveFontSize.sp,
                    color = activeColor,"""
replacement3 = """                    text = "Baris lirik yang sedang diputar",
                    fontFamily = customFontFamily,
                    fontSize = lyricsActiveFontSize.sp,
                    color = activeColor,"""
content = content.replace(target3, replacement3)

target4 = """                        text = "Baris lirik selanjutnya...",
                        fontSize = staticFontSizeOther,
                        color = otherColor.copy(alpha = otherAlpha),"""
replacement4 = """                        text = "Baris lirik selanjutnya...",
                        fontFamily = customFontFamily,
                        fontSize = staticFontSizeOther,
                        color = otherColor.copy(alpha = otherAlpha),"""
content = content.replace(target4, replacement4)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
