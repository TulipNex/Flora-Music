with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.filled.Add", "Icons.Default.Add")
content = content.replace("import androidx.compose.material.icons.filled.Check\n", "import androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Add\n")

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
