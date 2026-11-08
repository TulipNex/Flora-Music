import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace("Spacer(modifier = Modifier.width(16.dp))", "Spacer(modifier = Modifier.width(8.dp))")

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
