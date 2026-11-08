with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

start = content.find("fun FullScreenLyricsView")
end = content.find("fun SynchronizedLyricsLayout", start)

print(content[start:start+4000])
