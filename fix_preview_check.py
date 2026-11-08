with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

print("Preview Fixed at top?", "Preview Window - Fixed at top" in content)
