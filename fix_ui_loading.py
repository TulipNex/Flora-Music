import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

# Fix adding custom items to currentHidden
content = content.replace(
    """            if (item.role == TabRole.HIDDEN) {
                if (!item.isCustom) {
                    currentHidden.add(item.id)
                }
                continue
            }""",
    """            if (item.role == TabRole.HIDDEN) {
                currentHidden.add(item.id)
                continue
            }"""
)

# Fix loading from hiddenModules
hidden_loading = """
        for (hidden in hiddenModules) {
            if (hidden.startsWith("CUSTOM_")) {
                list.add(TabEditItem(hidden, hidden.removePrefix("CUSTOM_"), TabRole.HIDDEN, true))
            } else {
                list.add(TabEditItem(hidden, hidden, TabRole.HIDDEN, false))
            }
        }
"""

content = re.sub(
    r'        for \(hidden in hiddenModules\) \{\s*list\.add\(TabEditItem\(hidden, hidden, TabRole\.HIDDEN, false\)\)\s*\}',
    hidden_loading.strip(),
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
