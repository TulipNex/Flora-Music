with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "fun FullPlayerScreen" in line:
        start_idx = i
        break

balance = 0
found = False
for i in range(start_idx, len(lines)):
    line = lines[i]
    clean_line = line.split('//')[0]
    balance += clean_line.count('{')
    balance -= clean_line.count('}')
    if balance > 0:
        found = True
    if found and balance == 0:
        print(f"FullPlayerScreen closes at {i+1}")
        break

