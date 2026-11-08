import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'if (f.exists() && f.canWrite()) {',
    'if (f.exists()) {'
)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
