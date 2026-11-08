with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

print("FullScreenLyricsView modified?", "val lyricsSameFontSize" in content)
print("Loop modified?", "if (animatedAlpha > 0.01f)" in content)
