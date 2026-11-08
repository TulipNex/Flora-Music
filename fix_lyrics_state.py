with open('app/src/main/java/com/example/lyrics/LyricsEngine.kt', 'r') as f:
    content = f.read()

content = content.replace("sealed class LyricState {", "sealed class LyricState {\n    object Loading : LyricState()")

with open('app/src/main/java/com/example/lyrics/LyricsEngine.kt', 'w') as f:
    f.write(content)
