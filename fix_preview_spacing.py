with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace("verticalArrangement = Arrangement.spacedBy(12.dp)", "verticalArrangement = Arrangement.spacedBy(lyricsItemSpacing.dp)")

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
