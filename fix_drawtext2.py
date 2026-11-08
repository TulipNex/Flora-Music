import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Replace the nativeCanvas.drawText with explicit variables
old = 'canvas.nativeCanvas.drawText(letterLayouts[i].layoutInput.text.text, 0f, letterLayouts[i].firstBaseline, glowPaint)'
new = '''val textToDraw = letterLayouts[i].layoutInput.text.text
                                    val yPos = letterLayouts[i].firstBaseline
                                    canvas.nativeCanvas.drawText(textToDraw, 0f, yPos, glowPaint)'''
content = content.replace(old, new)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
