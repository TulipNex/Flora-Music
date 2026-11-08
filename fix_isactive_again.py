with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace('import kotlinx.coroutines.isActive\n', '')

import_section_end = content.find('\n@')
content = content[:import_section_end] + '\nimport kotlinx.coroutines.isActive' + content[import_section_end:]

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
