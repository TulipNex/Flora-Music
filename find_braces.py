with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

balance = 0
for i, line in enumerate(lines):
    balance += line.count('{')
    balance -= line.count('}')
    if balance < 0:
        print(f"Negative balance at line {i+1}: {line.strip()}")
        break
    if i % 500 == 0:
        print(f"Line {i+1}, balance: {balance}")

print(f"Final balance: {balance}")
