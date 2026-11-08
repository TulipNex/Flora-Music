import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# We need to add artDominantColor inside FullScreenLyricsView and LyricsTab 
# First, let's locate FullScreenLyricsView
target_fullscreen = """@Composable
fun FullScreenLyricsView(
    viewModel: MusicViewModel,
    song: Song,
    isPlaying: Boolean,
    playbackPosition: Long,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onCloseLyrics: () -> Unit
) {"""

replacement_fullscreen = target_fullscreen + """
    var songArtBytes by remember(song.path) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.path) {
        songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.data.scanner.MusicScanner.getArtworkBytes(song.path)
        }
    }
    val artBitmap = remember(songArtBytes) {
        songArtBytes?.let {
            try { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) } catch (e: Exception) { null }
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
"""

content = content.replace(target_fullscreen, replacement_fullscreen)

target_lyricstab = """@Composable
fun LyricsTab(
    viewModel: MusicViewModel,
    song: Song?,
    isPlaying: Boolean,
    playbackPosition: Long,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit
) {"""

replacement_lyricstab = target_lyricstab + """
    var songArtBytes by remember(song?.path) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song?.path) {
        song?.path?.let {
            songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.example.data.scanner.MusicScanner.getArtworkBytes(it)
            }
        }
    }
    val artBitmap = remember(songArtBytes) {
        songArtBytes?.let {
            try { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) } catch (e: Exception) { null }
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
"""

content = content.replace(target_lyricstab, replacement_lyricstab)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
