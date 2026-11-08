with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "fun SynchronizedLyricsLayout" in line:
        start_line = i
        break

balance = 0
for i in range(start_line, len(lines)):
    line = lines[i]
    balance += line.count('{')
    balance -= line.count('}')
    if balance == 0 and i > start_line + 5:
        print(f"SynchronizedLyricsLayout closes at line {i+1}")
        break

print(f"Balance after SynchronizedLyricsLayout (or EOF): {balance}")
