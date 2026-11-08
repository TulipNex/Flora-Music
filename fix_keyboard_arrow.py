import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target = "import androidx.compose.material.icons.filled.*"
replacement = "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight"
content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
