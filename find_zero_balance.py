with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()
b = 0
for i, line in enumerate(lines):
    b += line.count('{')
    b -= line.count('}')
    if i > 1565 and b == 0:
        print(f"Balance reaches 0 at line {i+1}")
        break
