with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

def find_closing_brace(start_line):
    balance = 0
    found = False
    for i in range(start_line, len(lines)):
        line = lines[i]
        clean_line = line.split('//')[0]
        balance += clean_line.count('{')
        balance -= clean_line.count('}')
        if balance > 0:
            found = True
        if found and balance == 0:
            return i
    return -1

root_box_idx = 194 # the line with `) {`
print(f"Root box closes at {find_closing_brace(root_box_idx)}")
