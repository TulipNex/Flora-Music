import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

replacement = """                                        val nextRole = when (item.role) {
                                            TabRole.MAIN -> TabRole.SUB
                                            else -> TabRole.MAIN
                                        }"""

content = re.sub(
    r'                                        val nextRole = when \(item\.role\) \{\s*TabRole\.MAIN -> TabRole\.SUB\s*TabRole\.SUB -> TabRole\.HIDDEN\s*TabRole\.HIDDEN -> TabRole\.MAIN\s*\}',
    replacement,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
