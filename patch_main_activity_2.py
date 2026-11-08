with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.action == android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) {"""
replacement = """    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.action == android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH || 
            intent?.action == android.content.Intent.ACTION_SEARCH) {"""
if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Patched main activity")
