import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace('com.example.domain.model.MusicScanner.getArtworkBytes', 'com.example.player.MusicScanner.getArtworkBytes')

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace('com.example.domain.model.MusicScanner.getArtworkBytes', 'com.example.player.MusicScanner.getArtworkBytes')

# Also fix the missing artDominantColor in LyricsTab
# Since it couldn't find the target, let's insert it right after the parsedLyrics in LyricsTab
target_lyricstab2 = """fun LyricsTab(
    viewModel: MusicViewModel,
    song: Song?,
    isPlaying: Boolean,
    playbackPosition: Long,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit
) {
    val parsedLyrics = remember(song?.lyricsSynced, song?.lyricsEmbedded) {
        val lrcSource = song?.lyricsSynced ?: song?.lyricsEmbedded
        if (lrcSource != null) LyricsParser.parse(lrcSource) else emptyList()
    }"""

replacement_lyricstab2 = target_lyricstab2 + """
    
    var songArtBytes2 by remember(song?.path) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song?.path) {
        song?.path?.let {
            songArtBytes2 = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.example.player.MusicScanner.getArtworkBytes(it)
            }
        }
    }
    val artBitmap2 = remember(songArtBytes2) {
        songArtBytes2?.let {
            try { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) } catch (e: Exception) { null }
        }
    }
    var artDominantColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
    LaunchedEffect(artBitmap2) {
        if (artBitmap2 != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val palette = androidx.palette.graphics.Palette.from(artBitmap2).generate()
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
"""

if target_lyricstab2 in content:
    content = content.replace(target_lyricstab2, replacement_lyricstab2)
    print("Fixed LyricsTab")
else:
    print("Could not find LyricsTab target")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

