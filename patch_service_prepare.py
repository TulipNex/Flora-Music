with open('app/src/main/java/com/example/player/MusicService.kt', 'r') as f:
    content = f.read()

target = """                override fun onPlayFromSearch(query: String?, extras: android.os.Bundle?) {"""
replacement = """                override fun onPrepareFromSearch(query: String?, extras: android.os.Bundle?) {
                    // Pre-load logic if necessary. Google Assistant sometimes calls this first.
                }

                override fun onPlayFromSearch(query: String?, extras: android.os.Bundle?) {"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
        f.write(content)
    print("Patched MusicService prepare")
