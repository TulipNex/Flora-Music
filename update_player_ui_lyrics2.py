import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace("lineHeight = (baseFontSize * 1.5f).sp", "lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
