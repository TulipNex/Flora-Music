import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace("fun SongMetadataDetailsDialog(", "fun SongMetadataEditScreen(")
content = content.replace("SongMetadataDetailsDialog(", "SongMetadataEditScreen(")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
