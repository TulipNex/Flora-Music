with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()
content = content.replace('}\n}\n}\n}\n// Dialog definitions', '}\n}\n}\n// Dialog definitions')
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
