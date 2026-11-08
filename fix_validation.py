import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

validation_logic = """
        viewModel.saveTabConfigs(newConfigs, currentHidden)
    }

    val validateAndSaveFn = {
        var hasMain = false
        for (item in allItems) {
            if (item.role == TabRole.MAIN) {
                hasMain = true
                break
            }
        }
        
        if (!hasMain) {
            val firstVisibleIdx = allItems.indexOfFirst { it.role != TabRole.HIDDEN }
            if (firstVisibleIdx != -1) {
                allItems[firstVisibleIdx] = allItems[firstVisibleIdx].copy(role = TabRole.MAIN)
            } else if (allItems.isNotEmpty()) {
                allItems[0] = allItems[0].copy(role = TabRole.MAIN)
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

content = content.replace("viewModel.saveTabConfigs(newConfigs, currentHidden)\n    }", validation_logic)

content = content.replace("saveFn(allItems)", "validateAndSaveFn()")

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
