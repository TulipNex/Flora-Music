with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace('glowPaint.textSize = lyricStyle.fontSize.toPx()', 'glowPaint.textSize = with(density) { lyricStyle.fontSize.toPx() }')

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
