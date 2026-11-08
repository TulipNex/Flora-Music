with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "lines.forEachIndexed" in line and i > 1400:
        start_idx = i
        break

# trace to find the closing brace for `forEachIndexed`
balance = 0
for i in range(start_idx, len(lines)):
    line = lines[i]
    clean_line = line.split('//')[0]
    balance += clean_line.count('{')
    balance -= clean_line.count('}')
    
    # since we ADDED one `{` (if (isVisible) {), the brace for `forEachIndexed` will be when balance == 1 !
    if balance == 1 and "}" in line and i > start_idx + 10:
        print(f"Should insert `}} // end isVisible` around line {i}")
        break
