import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

replacement = """
        list.add(TabEditItem("HEADER_HIDDEN", "Tab Tersembunyi", TabRole.HEADER, false))
        for (hidden in hiddenModules) {
            if (hidden.startsWith("CUSTOM_")) {
"""

content = content.replace('for (hidden in hiddenModules) {\n            if (hidden.startsWith("CUSTOM_")) {', replacement.strip())

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
