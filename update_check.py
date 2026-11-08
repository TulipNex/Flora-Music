import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

target = """        val hasInvalidSubtab = configs.any { config ->
            config.items.any { item ->
                item in invalidSubtabs && (config.isCustom || item != config.name)
            }
        }"""

replacement = """        val hasInvalidSubtab = configs.any { config ->
            val isSubtab = config.items.any { item ->
                item in invalidSubtabs && (config.isCustom || item != config.name)
            }
            val hasSubtabs = config.name in invalidSubtabs && config.items.size > 1
            isSubtab || hasSubtabs
        }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
