with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

import_statement = "import androidx.compose.ui.focus.focusRequester\n"
if import_statement not in content:
    content = content.replace("import androidx.compose.ui.Modifier\n", "import androidx.compose.ui.Modifier\n" + import_statement)
    with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
        f.write(content)
