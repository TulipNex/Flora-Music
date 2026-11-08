import re

with open('app/src/main/java/com/example/player/MusicService.kt', 'r') as f:
    content = f.read()

target = """            val songArtBytes = try {
                MusicScanner.getArtworkBytes(song.path)
            } catch (e: Exception) {
                null
            }"""

replacement = """            val songArtBytes = try {
                if (!song.customCoverUri.isNullOrEmpty()) {
                    try {
                        contentResolver.openInputStream(android.net.Uri.parse(song.customCoverUri))?.use { it.readBytes() }
                    } catch(e: Exception) {
                        MusicScanner.getArtworkBytes(song.path)
                    }
                } else {
                    MusicScanner.getArtworkBytes(song.path)
                }
            } catch (e: Exception) {
                null
            }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
    f.write(content)
