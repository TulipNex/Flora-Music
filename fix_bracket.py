import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Replace removePrefix("(").removeSuffix(")") with regex replace
old_bracket = 'val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text'
new_bracket = 'val displayText = if (line.isBackground) line.text.replace(Regex("^\\\\s*\\\\(|\\\\)\\\\s*$"), "").trim() else line.text'
content = content.replace(old_bracket, new_bracket)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
