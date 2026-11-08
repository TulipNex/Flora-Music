with open('app/src/main/java/com/example/player/MusicService.kt', 'r') as f:
    content = f.read()

import_browser = "import androidx.media.MediaBrowserServiceCompat\nimport android.support.v4.media.MediaBrowserCompat\n"
if "MediaBrowserServiceCompat" not in content:
    content = content.replace("class MusicService : Service() {", import_browser + "class MusicService : MediaBrowserServiceCompat() {")

    target_oncreate = """        mediaSession = MediaSessionCompat(this, "FloraMusicService").apply {"""
    replacement_oncreate = """        mediaSession = MediaSessionCompat(this, "FloraMusicService").apply {
            setSessionToken(sessionToken)"""
    content = content.replace(target_oncreate, replacement_oncreate)

    # Add overrides at the end of class before the last brace
    overrides = """
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(mutableListOf())
    }
}"""
    content = content.replace("}\n}", "}\n" + overrides)
    
    with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Already updated")
