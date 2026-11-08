import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """    val hiddenModules by viewModel.hiddenModules.collectAsState()
    val customTabIcons by viewModel.customTabIcons.collectAsState()
    
    var iconPickerItem by remember { mutableStateOf<TabEditItem?>(null) }

    val allItems = remember(tabConfigs, hiddenModules) {"""

replacement1 = """    val hiddenModules by viewModel.hiddenModules.collectAsState()
    val customTabIcons by viewModel.customTabIcons.collectAsState()
    
    var iconPickerItem by remember { mutableStateOf<TabEditItem?>(null) }
    var forceRefresh by remember { mutableStateOf(0) }

    val allItems = remember(tabConfigs, hiddenModules, forceRefresh) {"""

content = content.replace(target1, replacement1)

target2 = """        if (currentConfigName.isNotEmpty()) {
            newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
        }
        
        
        viewModel.saveTabConfigs(newConfigs, currentHidden)
    }

    val validateAndSaveFn = {"""

replacement2 = """        if (currentConfigName.isNotEmpty()) {
            newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
        }
        
        
        return@val viewModel.saveTabConfigs(newConfigs, currentHidden)
    }

    val validateAndSaveFn = {"""

content = content.replace(target2, replacement2)

target3 = """        var seenMain = false
        var currentMainName = ""
        for (i in allItems.indices) {
            val item = allItems[i]
            if (item.role == TabRole.MAIN) {
                seenMain = true
                currentMainName = item.name
            } else if (item.role == TabRole.SUB) {
                if (!seenMain || currentMainName in listOf("Lagu", "Artis", "Album")) {
                    allItems[i] = item.copy(role = TabRole.MAIN)
                    seenMain = true
                    currentMainName = item.name
                }
            }
        }
        
        saveFn(allItems)
    }"""

replacement3 = """        var seenMain = false
        var currentMainName = ""
        for (i in allItems.indices) {
            val item = allItems[i]
            if (item.role == TabRole.MAIN) {
                seenMain = true
                currentMainName = item.name
            } else if (item.role == TabRole.SUB) {
                if (!seenMain || currentMainName in listOf("Lagu", "Artis", "Album")) {
                    allItems[i] = item.copy(role = TabRole.MAIN)
                    seenMain = true
                    currentMainName = item.name
                }
            }
        }
        
        val success = saveFn(allItems)
        if (!success) {
            forceRefresh++
        }
    }"""

content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
