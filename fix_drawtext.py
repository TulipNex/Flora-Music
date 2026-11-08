import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace('drawText(layoutResult', 'drawText(textLayoutResult = layoutResult')
content = content.replace('drawText(letterLayouts[i]', 'drawText(textLayoutResult = letterLayouts[i]')

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
