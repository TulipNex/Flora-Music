with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if i == 166 and "validateAndSaveFn()" in line:
        lines[i] = line.replace("validateAndSaveFn()", "saveFn(allItems)")

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.writelines(lines)
