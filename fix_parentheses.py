import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Fix in FullScreenLyricsView
old_view1 = '''                                    }) {
                                        if (line.words.isNotEmpty()) {
                                            FloraWordLevelLyrics(
                                                mainText = line.text,'''
new_view1 = '''                                    }) {
                                        val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
                                        if (line.words.isNotEmpty()) {
                                            FloraWordLevelLyrics(
                                                mainText = displayText,'''

old_view2 = '''                                        } else {
                                            Text(
                                                text = line.text,'''
new_view2 = '''                                        } else {
                                            Text(
                                                text = displayText,'''

content = content.replace(old_view1, new_view1)
content = content.replace(old_view2, new_view2)

# Fix in SynchronizedLyricsLayout (It's identical code, so the replace above might do both, but let's check count)
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
