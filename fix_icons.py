import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight", "androidx.compose.material.icons.Icons.Filled.KeyboardArrowRight")

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)

