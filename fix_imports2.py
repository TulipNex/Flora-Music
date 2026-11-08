with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

imports = [
    "import androidx.compose.ui.text.TextStyle",
    "import androidx.compose.foundation.Canvas",
    "import androidx.compose.ui.text.drawText",
    "import androidx.compose.ui.graphics.drawscope.clipRect",
    "import androidx.compose.ui.graphics.drawscope.withTransform",
    "import kotlin.math.exp",
    "import kotlin.math.cos",
    "import kotlin.math.PI",
    "import kotlin.math.sin",
    "import kotlin.math.abs",
    "import androidx.compose.ui.graphics.toArgb"
]

import_section_end = content.find('\n@')
if import_section_end == -1:
    import_section_end = content.find('\nfun')

existing_imports = content[:import_section_end]
for imp in imports:
    if imp not in existing_imports:
        existing_imports += '\n' + imp

content = existing_imports + content[import_section_end:]

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
