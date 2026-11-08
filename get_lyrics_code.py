import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

start = content.find("val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()")
end = content.find("val colAlign = when (lyricsAlignment)")
print(content[start:end])
