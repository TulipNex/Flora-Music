import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """        val activeColorStr = if (lyricsUseDominantColor) "#10B981" else lyricsActiveFontColor
        val otherColorStr = if (lyricsUseDominantColor) "#888888" else lyricsOtherFontColor
        val activeColor = try { Color(android.graphics.Color.parseColor(activeColorStr)) } catch(e: Exception) { Color.White }
        val otherColor = try { Color(android.graphics.Color.parseColor(otherColorStr)) } catch(e: Exception) { Color.Gray }"""

replacement = """        val currentSong by viewModel.playerController.currentSong.collectAsState()
        var songArtBytes by remember(currentSong?.path) { mutableStateOf<ByteArray?>(null) }
        LaunchedEffect(currentSong?.path) {
            currentSong?.path?.let {
                songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.domain.model.MusicScanner.getArtworkBytes(it)
                }
            }
        }
        val artBitmap = remember(songArtBytes) {
            songArtBytes?.let {
                try {
                    android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
                } catch (e: Exception) {
                    null
                }
            }
        }
        var artDominantColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
        LaunchedEffect(artBitmap) {
            if (artBitmap != null) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    try {
                        val palette = androidx.palette.graphics.Palette.from(artBitmap).generate()
                        val vibrant = palette.vibrantSwatch
                        val lightVibrant = palette.lightVibrantSwatch
                        val dominant = palette.dominantSwatch
                        val colorInt = lightVibrant?.rgb ?: vibrant?.rgb ?: dominant?.rgb ?: android.graphics.Color.WHITE
                        artDominantColor = androidx.compose.ui.graphics.Color(colorInt)
                    } catch (e: Exception) {
                        artDominantColor = androidx.compose.ui.graphics.Color.White
                    }
                }
            } else {
                artDominantColor = null
            }
        }

        val activeColor = if (lyricsUseDominantColor) {
            artDominantColor ?: Color(android.graphics.Color.parseColor("#10B981"))
        } else {
            try { Color(android.graphics.Color.parseColor(lyricsActiveFontColor)) } catch(e: Exception) { Color.White }
        }
        
        val otherColor = if (lyricsUseDominantColor) {
            (artDominantColor ?: Color(android.graphics.Color.parseColor("#10B981"))).copy(alpha = 0.5f)
        } else {
            try { Color(android.graphics.Color.parseColor(lyricsOtherFontColor)) } catch(e: Exception) { Color.Gray }
        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
        f.write(content)
    print("Settings UI replaced")
else:
    print("Settings UI Target not found")
