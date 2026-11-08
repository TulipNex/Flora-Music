import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target = """                    val icon = when (tab) {
                        "Lagu" -> Icons.Default.MusicNote"""

replacement = """                    val icon = when (tab) {
                        "Beranda" -> Icons.Default.Home
                        "Lagu" -> Icons.Default.MusicNote"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
