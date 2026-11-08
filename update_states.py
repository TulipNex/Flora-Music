import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace("var showPositionDialog by remember { mutableStateOf(false) }", "var showPositionDialog by remember { mutableStateOf(false) }\n    var showFadeDialog by remember { mutableStateOf(false) }")

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
