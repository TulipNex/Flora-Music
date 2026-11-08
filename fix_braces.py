with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "fun FullPlayerScreen" in line:
        start_line = i
        break

balance = 0
found_first_brace = False
for i in range(start_line, 1091):
    line = lines[i]
    clean_line = line.split('//')[0]
    balance += clean_line.count('{')
    balance -= clean_line.count('}')
    if balance > 0:
        found_first_brace = True

print(f"Final balance at line 1091: {balance}")
