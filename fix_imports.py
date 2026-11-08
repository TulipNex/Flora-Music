with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

imports = [
    "import androidx.compose.ui.graphics.nativeCanvas",
    "import androidx.compose.ui.graphics.drawscope.drawIntoCanvas",
    "import androidx.compose.ui.graphics.drawscope.scale",
    "import androidx.compose.ui.graphics.drawscope.translate",
    "import androidx.compose.ui.graphics.drawscope.withTransform",
    "import androidx.compose.ui.text.rememberTextMeasurer",
    "import androidx.compose.ui.text.drawText",
    "import android.graphics.BlurMaskFilter",
    "import java.text.BreakIterator",
    "import androidx.compose.ui.graphics.drawscope.clipRect",
    "import androidx.compose.ui.graphics.CompositingStrategy"
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
