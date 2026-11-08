with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

balance = 0
for i, line in enumerate(lines):
    clean_line = line.split('//')[0]
    balance += clean_line.count('{')
    balance -= clean_line.count('}')
    
print(f"End of file balance: {balance}")
