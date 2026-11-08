import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target = "    val isPlayerCompact by viewModel.isPlayerCompact.collectAsState()"
content = content.replace(target, "")

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
