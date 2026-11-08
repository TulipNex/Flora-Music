with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace("val data = repo.getLyrics(song)", "parsedLyrics = LyricState.Loading\n        val data = repo.getLyrics(song)")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
