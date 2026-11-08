import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import kotlinx.coroutines.launch',
    'import kotlinx.coroutines.launch\nimport kotlinx.coroutines.withContext\nimport kotlinx.coroutines.Dispatchers'
)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
