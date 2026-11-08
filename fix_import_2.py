import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.foundation.layout.Arrangement" not in content:
    content = content.replace(
        "import androidx.compose.foundation.layout.Row",
        "import androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Arrangement"
    )

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
