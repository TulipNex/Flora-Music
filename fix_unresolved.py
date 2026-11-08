import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

# Fix FloraLyricsSettingsUI.kt `data` reference
content = content.replace('com.example.data.scanner.MusicScanner', 'com.example.domain.model.MusicScanner')

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Fix FloraPlayerUI.kt `data` reference
content = content.replace('com.example.data.scanner.MusicScanner', 'com.example.domain.model.MusicScanner')

# Look around line 1490 and 1496 - artDominantColor is still missing there.
# Let's inspect where it's missing in LyricsTab
# I'll just remove artDominantColor calculation and revert to using the globally available one.
# But wait, there is no globally available one. They are separate Composables.
# We will just declare it again at the beginning of the LyricsTab body.
