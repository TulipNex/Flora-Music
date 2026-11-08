import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

new_validate = """
    val validateAndSaveFn = {
        val headerIdx = allItems.indexOfFirst { it.id == "HEADER_HIDDEN" }
        if (headerIdx != -1) {
            for (i in allItems.indices) {
                if (allItems[i].role == TabRole.HEADER) continue
                if (i < headerIdx) {
                    if (allItems[i].role == TabRole.HIDDEN) {
                        allItems[i] = allItems[i].copy(role = TabRole.MAIN)
                    }
                } else {
                    if (allItems[i].role != TabRole.HIDDEN) {
                        allItems[i] = allItems[i].copy(role = TabRole.HIDDEN)
                    }
                }
            }
        }

        var hasMain = false
        for (item in allItems) {
            if (item.role == TabRole.MAIN) {
                hasMain = true
                break
            }
        }
        
        if (!hasMain) {
            val firstVisibleIdx = allItems.indexOfFirst { it.role != TabRole.HIDDEN && it.role != TabRole.HEADER }
            if (firstVisibleIdx != -1) {
                allItems[firstVisibleIdx] = allItems[firstVisibleIdx].copy(role = TabRole.MAIN)
            }
        }

        var seenMain = false
        for (i in allItems.indices) {
            val item = allItems[i]
            if (item.role == TabRole.MAIN) {
                seenMain = true
            } else if (item.role == TabRole.SUB) {
                if (!seenMain) {
                    allItems[i] = item.copy(role = TabRole.MAIN)
                    seenMain = true
                }
            }
        }
        
        saveFn(allItems)
    }
"""

content = re.sub(r'    val validateAndSaveFn = \{.*?        saveFn\(allItems\)\n    \}', new_validate.strip('\n'), content, flags=re.DOTALL)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
