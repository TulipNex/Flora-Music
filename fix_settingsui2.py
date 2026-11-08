import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """    val customFontFamily = remember(lyricsFontFamily, lyricsCustomFontUri) {
        if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(lyricsCustomFontUri)
                val context = androidx.compose.ui.platform.LocalContext.current
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")"""
replacement = """    val customFontFamilyContext = androidx.compose.ui.platform.LocalContext.current
    val customFontFamily = remember(lyricsFontFamily, lyricsCustomFontUri, customFontFamilyContext) {
        if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(lyricsCustomFontUri)
                val pfd = customFontFamilyContext.contentResolver.openFileDescriptor(uri, "r")"""
                
content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
