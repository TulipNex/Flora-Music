import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()
content = content.replace('canvas.nativeCanvas.drawText(textLayoutResult = letterLayouts[i].layoutInput.text.text, 0f, letterLayouts[i].firstBaseline, glowPaint)', 'val textToDraw = letterLayouts[i].layoutInput.text.text\n                                    val yPos = letterLayouts[i].firstBaseline\n                                    canvas.nativeCanvas.drawText(textToDraw, 0f, yPos, glowPaint)')
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
