with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace("var parsedLyrics by remember { mutableStateOf<LyricState>(LyricState.None) }", "var parsedLyrics by remember(song.id) { mutableStateOf<LyricState>(LyricState.Loading) }")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
