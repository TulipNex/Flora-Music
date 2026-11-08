import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace('fontSize = (if (line.isBackground) baseFontSize * 0.75f else baseFontSize).sp,',
                          'fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,')
content = content.replace('fontSize = (if (line.isBackground) baseFontSize * 0.75f else baseFontSize).sp,',
                          'fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,')

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
