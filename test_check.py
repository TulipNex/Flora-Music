with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()
print("lyricsContextLines defined?", "val lyricsContextLines by viewModel.lyricsContextLines.collectAsState()" in content)
print("lyricsFadeEffect defined?", "val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()" in content)
print("absDist defined?", "val absDist = kotlin.math.abs(distance)" in content)
