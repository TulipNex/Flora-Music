import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# I want to find the itemsIndexed block of FullScreenLyricsView
# It starts around line 1245
