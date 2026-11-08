import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """fun MetaRow(label: String, value: String) {"""
replacement = """@Composable\nfun MetaRow(label: String, value: String) {"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
