import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

# Fix saveFn
target_save_fn = """            if (item.role == TabRole.MAIN) {
                if (currentConfigName.isNotEmpty()) {
                    newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
                }
                currentConfigName = item.name
                currentConfigItems = mutableListOf()
                currentIsCustom = item.isCustom
                if (!item.isCustom) {
                    currentConfigItems.add(item.id)
                }
            } else if (item.role == TabRole.SUB) {
                if (currentConfigName.isNotEmpty()) {
                    if (!item.isCustom) {
                        currentConfigItems.add(item.id)
                    }
                } else {
                    currentConfigName = item.name
                    currentConfigItems = mutableListOf()
                    currentIsCustom = item.isCustom
                    if (!item.isCustom) {
                        currentConfigItems.add(item.id)
                    }
                }
            }"""

replacement_save_fn = """            if (item.role == TabRole.MAIN) {
                if (currentConfigName.isNotEmpty()) {
                    newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
                }
                currentConfigName = item.name
                currentConfigItems = mutableListOf()
                currentIsCustom = item.isCustom
                if (!item.isCustom) {
                    currentConfigItems.add(item.id)
                }
            } else if (item.role == TabRole.SUB) {
                if (currentConfigName.isNotEmpty()) {
                    currentConfigItems.add(item.id)
                } else {
                    currentConfigName = item.name
                    currentConfigItems = mutableListOf()
                    currentIsCustom = item.isCustom
                    currentConfigItems.add(item.id)
                }
            }"""

content = content.replace(target_save_fn, replacement_save_fn)

# Fix allItems remember block
target_remember = """        for (config in tabConfigs) {
            if (config.isCustom) {
                list.add(TabEditItem("CUSTOM_${config.name}", config.name, TabRole.MAIN, true))
                for (item in config.items) {
                    list.add(TabEditItem(item, item, TabRole.SUB, false))
                }
            } else {
                list.add(TabEditItem(config.name, config.name, TabRole.MAIN, false))
                for (item in config.items) {
                    if (item != config.name) {
                        list.add(TabEditItem(item, item, TabRole.SUB, false))
                    }
                }
            }
        }"""

replacement_remember = """        for (config in tabConfigs) {
            if (config.isCustom) {
                list.add(TabEditItem("CUSTOM_${config.name}", config.name, TabRole.MAIN, true))
                for (item in config.items) {
                    if (item.startsWith("CUSTOM_")) {
                        list.add(TabEditItem(item, item.removePrefix("CUSTOM_"), TabRole.SUB, true))
                    } else {
                        list.add(TabEditItem(item, item, TabRole.SUB, false))
                    }
                }
            } else {
                list.add(TabEditItem(config.name, config.name, TabRole.MAIN, false))
                for (item in config.items) {
                    if (item.startsWith("CUSTOM_")) {
                        list.add(TabEditItem(item, item.removePrefix("CUSTOM_"), TabRole.SUB, true))
                    } else if (item != config.name) {
                        list.add(TabEditItem(item, item, TabRole.SUB, false))
                    }
                }
            }
        }"""

content = content.replace(target_remember, replacement_remember)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
