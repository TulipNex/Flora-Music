with open('app/src/main/java/com/example/player/MusicService.kt', 'r') as f:
    content = f.read()

target = """                        PlaybackStateCompat.ACTION_SEEK_TO
                    )"""

replacement = """                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                    )"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Target not found")
