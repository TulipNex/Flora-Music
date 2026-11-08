with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()
if 'import kotlinx.coroutines.isActive' not in content:
    content = 'import kotlinx.coroutines.isActive\n' + content
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
