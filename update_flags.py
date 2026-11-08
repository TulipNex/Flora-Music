with open('app/src/main/java/com/example/player/MusicService.kt', 'r') as f:
    content = f.read()

target = """        mediaSession = MediaSessionCompat(this, "FloraMusicService").apply {
            this@MusicService.sessionToken = sessionToken
            isActive = true"""
replacement = """        mediaSession = MediaSessionCompat(this, "FloraMusicService").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            this@MusicService.sessionToken = sessionToken
            isActive = true"""
if target in content:
    content = content.replace(target, replacement)
    print("Replaced flags")

target2 = """                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                    )"""
replacement2 = """                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID
                    )"""
if target2 in content:
    content = content.replace(target2, replacement2)
    print("Replaced actions")

with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
    f.write(content)
