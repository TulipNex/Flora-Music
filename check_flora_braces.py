with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

start = -1
for i, line in enumerate(lines):
    if "fun FloraWordLevelLyrics" in line:
        start = i
        break

b = 0
for i in range(start, len(lines)):
    b += lines[i].count('{')
    b -= lines[i].count('}')

print(f"FloraWordLevelLyrics balance: {b}")
