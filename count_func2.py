with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

balance = 0
for i in range(1532, 1914):
    line = lines[i]
    clean_line = line.split('//')[0]
    balance += clean_line.count('{')
    balance -= clean_line.count('}')
    if balance != 0:
        print(f"Non-zero balance at {i+1}: {balance}")
        break

print(f"Balance at 1914: {balance}")
