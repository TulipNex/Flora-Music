with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace('import kotlinx.coroutines.isActive\npackage com.example.presentation\n', 'package com.example.presentation\nimport kotlinx.coroutines.isActive\n')

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
