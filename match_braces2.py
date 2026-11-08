with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

def find_closing_brace(start_line):
    balance = 0
    for i in range(start_line, len(lines)):
        line = lines[i]
        clean_line = line.split('//')[0]
        balance += clean_line.count('{')
        balance -= clean_line.count('}')
        if balance == 0:
            return i
    return -1

for i, line in enumerate(lines):
    if "fun FullPlayerScreen" in line:
        start_line = i
        break

for i in range(start_line, len(lines)):
    if "Box(" in lines[i] and "Modifier.fillMaxSize()" in lines[i]:
        root_box_idx = i
        break
        
print(f"Root box starts at {root_box_idx}, closes at {find_closing_brace(root_box_idx)}")
