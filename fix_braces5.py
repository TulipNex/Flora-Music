with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "fun FullScreenLyricsView" in line:
        start_line = i
        break

balance = 0
found_first = False
for i in range(start_line, len(lines)):
    line = lines[i]
    clean_line = line.split('//')[0]
    balance += clean_line.count('{')
    balance -= clean_line.count('}')
    
    if balance > 0:
        found_first = True
    if found_first and balance == 0:
        print(f"FullScreenLyricsView closes at {i+1}")
        break

if balance != 0:
    print(f"Balance never reached 0. End balance: {balance}")
