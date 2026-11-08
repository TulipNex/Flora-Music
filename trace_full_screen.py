with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()
b = 0
for i in range(1088, 1560):
    line = lines[i]
    b += line.count('{')
    b -= line.count('}')
    if b == 0 and i > 1100:
        print(f"FullScreenLyricsView closes at {i+1}")
        break
print(f"Balance at 1560: {b}")
