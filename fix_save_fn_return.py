import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

target = """        if (currentConfigName.isNotEmpty()) {
            newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
        }
        
        
        return@val viewModel.saveTabConfigs(newConfigs, currentHidden)
    }"""

replacement = """        if (currentConfigName.isNotEmpty()) {
            newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
        }
        
        viewModel.saveTabConfigs(newConfigs, currentHidden)
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
