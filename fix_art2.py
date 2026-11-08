import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """    var songArtBytes by remember(song.path) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.path) {
        songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.utils.MusicScanner.getArtworkBytes(song.path)
        }
    }"""

replacement = """    val contextLocal = LocalContext.current
    var songArtBytes by remember(song.path, song.customCoverUri) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.path, song.customCoverUri) {
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
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
