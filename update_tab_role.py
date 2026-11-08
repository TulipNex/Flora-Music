with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'enum class TabRole(val label: String) {\n    MAIN("Utama"),\n    SUB("Subtab"),\n    HIDDEN("Sembunyi")\n}',
    'enum class TabRole(val label: String) {\n    MAIN("Utama"),\n    SUB("Subtab"),\n    HIDDEN("Sembunyi"),\n    HEADER("Header")\n}'
)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
