with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'r') as f:
    content = f.read()

old_code = """    private fun startPlaybackService() {
        try {
            val intent = Intent(context, MusicService::class.java).apply {
                action = MusicService.ACTION_START_FOREGROUND
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start foreground service: ${e.message}")
        }
    }"""
new_code = """    private fun startPlaybackService() {
        try {
            val intent = Intent(context, MusicService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start foreground service: ${e.message}")
        }
    }"""

if old_code in content:
    content = content.replace(old_code, new_code)
    with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'w') as f:
        f.write(content)
    print("Patched startPlaybackService")
else:
    print("startPlaybackService not found exactly")
