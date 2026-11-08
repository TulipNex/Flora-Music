import sys

with open('app/src/main/java/com/example/player/MusicService.kt', 'r') as f:
    content = f.read()

import_bundle = "import android.os.Bundle\n"
if import_bundle not in content:
    content = content.replace("import android.os.Build", import_bundle + "import android.os.Build")

target = """    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("root", null)
    }"""
replacement = """    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot("root", null)
    }"""
content = content.replace(target, replacement)

target_token = """        mediaSession = MediaSessionCompat(this, "FloraMusicService").apply {
            setSessionToken(sessionToken)"""
replacement_token = """        mediaSession = MediaSessionCompat(this, "FloraMusicService").apply {
            this@MusicService.sessionToken = sessionToken"""
content = content.replace(target_token, replacement_token)

with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
    f.write(content)

print("Fixed service overrides")
