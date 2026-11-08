import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace(".padding(horizontal = 16.dp, vertical = 10.dp)", ".padding(horizontal = 16.dp, vertical = 2.dp)")

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
