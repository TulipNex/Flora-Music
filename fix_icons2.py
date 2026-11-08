import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

# Let's replace the inline KeyboardArrowRight usage with `Icons.Default.KeyboardArrowRight` and add the import.
content = content.replace("androidx.compose.material.icons.Icons.Filled.KeyboardArrowRight", "Icons.Default.KeyboardArrowRight")

import_target = "import androidx.compose.material.icons.filled.Check"
import_replacement = "import androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.KeyboardArrowRight"
if "import androidx.compose.material.icons.filled.KeyboardArrowRight" not in content:
    content = content.replace(import_target, import_replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)

