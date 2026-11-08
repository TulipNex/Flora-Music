with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'r') as f:
    content = f.read()

# Expose exoPlayer
content = content.replace('private var exoPlayer: ExoPlayer', 'val player: ExoPlayer get() = exoPlayer\n    private var exoPlayer: ExoPlayer')

# Modify mediaItem generation in play()
old_map = """        val mediaItems = playQueue.map {
            MediaItem.fromUri(Uri.fromFile(java.io.File(it.path)))
        }"""
new_map = """        val mediaItems = playQueue.map { s ->
            val metadata = androidx.media3.common.MediaMetadata.Builder()
                .setTitle(s.title)
                .setArtist(s.artist)
                .setAlbumTitle(s.album)
                .setArtworkUri(if (!s.customCoverUri.isNullOrEmpty()) Uri.parse(s.customCoverUri) else null)
                .build()
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(Uri.fromFile(java.io.File(s.path)))
                .setMediaMetadata(metadata)
                .build()
        }"""
content = content.replace(old_map, new_map)

with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'w') as f:
    f.write(content)
print("Patched MusicPlayerController.kt")
