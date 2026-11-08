with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace("var showFadeDialog by remember { mutableStateOf(false) }", "var showFadeDialog by remember { mutableStateOf(false) }\n    var showActiveColorDialog by remember { mutableStateOf(false) }\n    var showInactiveColorDialog by remember { mutableStateOf(false) }\n    var customActiveColorInput by remember { mutableStateOf(lyricsActiveFontColor) }\n    var customInactiveColorInput by remember { mutableStateOf(lyricsOtherFontColor) }")

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
