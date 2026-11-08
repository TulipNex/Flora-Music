import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = "import androidx.compose.ui.graphics.Color"
replacement = "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.luminance\nimport androidx.compose.material.icons.filled.Check"
content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
