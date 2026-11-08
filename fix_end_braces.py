with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()
content = content.replace('}\n}\nprivate data class HyphenGroupWord', '}\nprivate data class HyphenGroupWord')
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
