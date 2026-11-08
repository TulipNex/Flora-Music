with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

b = 0
for i in range(1560, 1928):
    line = lines[i]
    b += line.count('{')
    b -= line.count('}')

print(f"Open braces before line 1928: {b}")
