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
    if "Box(modifier = Modifier.fillMaxSize()) {" in line and i < 300:
        outer_box_idx = i
    if "Box(modifier = Modifier.fillMaxSize().graphicsLayer" in line and i < 300:
        inner_box_idx = i
        break

outer_close = find_closing_brace(outer_box_idx)
inner_close = find_closing_brace(inner_box_idx)

print(f"Outer box starts at {outer_box_idx}, closes at {outer_close}")
print(f"Inner box starts at {inner_box_idx}, closes at {inner_close}")
