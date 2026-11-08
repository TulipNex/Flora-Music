with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace('// 11. SYNCHRONIZED LYRICS VIEWER COMPOSABLE', '}\n// 11. SYNCHRONIZED LYRICS VIEWER COMPOSABLE')

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
