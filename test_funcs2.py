with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

balance = 0
for i, line in enumerate(lines):
    clean_line = line.split('//')[0]
    
    if balance == 0 and "fun " in clean_line:
        print(f"Top level func starts at {i+1}: {clean_line.strip()}")
        
    balance += clean_line.count('{')
    balance -= clean_line.count('}')
    
    if balance == 0 and "}" in clean_line:
        print(f"Top level ends at {i+1}")
