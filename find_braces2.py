with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()
b = 0
for i, line in enumerate(lines):
    b += line.count('{')
    b -= line.count('}')
    if i == 1928:
        print(f"Line 1929 balance: {b}")
