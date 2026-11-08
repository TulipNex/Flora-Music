with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()
b = 0
for i in range(0, 1088):
    b += lines[i].count('{')
    b -= lines[i].count('}')
print(f"Balance at 1088: {b}")
