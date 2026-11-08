with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

count = 0
for i in range(251, 1042):
    line = lines[i]
    count += line.count('{')
    count -= line.count('}')

print(f"Brace balance from 252 to 1042 is: {count}")
