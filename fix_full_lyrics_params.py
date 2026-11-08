with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Replace the definition
old_def = """fun FullScreenLyricsView(
    viewModel: MusicViewModel,
    song: Song,
    isPlaying: Boolean,
    playbackPosition: Long,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onCloseLyrics: () -> Unit
) {"""
new_def = """fun FullScreenLyricsView(
    viewModel: MusicViewModel,
    song: Song,
    isPlaying: Boolean,
    playbackPosition: Long,
    artBitmap: android.graphics.Bitmap?,
    artDominantColor: androidx.compose.ui.graphics.Color?,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onCloseLyrics: () -> Unit
) {"""
content = content.replace(old_def, new_def)

# Remove the internal loading logic in FullScreenLyricsView
old_logic = """    val contextLocal = LocalContext.current
    var songArtBytes by remember(song.path, song.customCoverUri, song.dateModified) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.path, song.customCoverUri, song.dateModified) {
        songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (!song.customCoverUri.isNullOrEmpty()) {
                try {
                    contextLocal.contentResolver.openInputStream(Uri.parse(song.customCoverUri))?.use { it.readBytes() }
                } catch(e: Exception) {
                    com.example.utils.MusicScanner.getArtworkBytes(song.path)
                }
            } else {
                com.example.utils.MusicScanner.getArtworkBytes(song.path)
            }
        }
    }
    var artBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(songArtBytes) {
        if (songArtBytes != null) {
            artBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try { android.graphics.BitmapFactory.decodeByteArray(songArtBytes!!, 0, songArtBytes!!.size) } catch (e: Exception) { null }
            }
        } else {
            artBitmap = null
        }
    }
    var artDominantColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
    LaunchedEffect(artBitmap) {
        if (artBitmap != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val palette = androidx.palette.graphics.Palette.from(artBitmap!!).generate()
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
    }"""
content = content.replace(old_logic, "    val contextLocal = LocalContext.current")

# Update the call site
old_call = """                FullScreenLyricsView(
                    viewModel = viewModel,
                    song = song,
                    isPlaying = isPlaying,
                    playbackPosition = playbackPosition,
                    onPlayPauseClick = { viewModel.playerController.togglePlay() },
                    onSeekTo = { viewModel.playerController.seekTo(it) },
                    onCloseLyrics = { showLyricsView = false }
                )"""
new_call = """                FullScreenLyricsView(
                    viewModel = viewModel,
                    song = song,
                    isPlaying = isPlaying,
                    playbackPosition = playbackPosition,
                    artBitmap = artBitmap,
                    artDominantColor = artDominantColor,
                    onPlayPauseClick = { viewModel.playerController.togglePlay() },
                    onSeekTo = { viewModel.playerController.seekTo(it) },
                    onCloseLyrics = { showLyricsView = false }
                )"""
content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
