import sys

# 1. Fix MusicService.kt
file1 = 'app/src/main/java/com/example/player/MusicService.kt'
with open(file1, 'r') as f:
    c1 = f.read()

import_stmt = "import kotlinx.coroutines.flow.firstOrNull\n"
if import_stmt not in c1:
    c1 = c1.replace("import kotlinx.coroutines.*", "import kotlinx.coroutines.*\n" + import_stmt)

c1 = c1.replace("repo.getSongsForPlaylist(p.id)", "repo.getSongsInPlaylist(p.id)")
with open(file1, 'w') as f:
    f.write(c1)

# 2. Fix MusicViewModel.kt
file2 = 'app/src/main/java/com/example/presentation/MusicViewModel.kt'
with open(file2, 'r') as f:
    c2 = f.read()

c2 = c2.replace("repository.getSongsForPlaylist(p.id)", "repository.getSongsInPlaylist(p.id)")
with open(file2, 'w') as f:
    f.write(c2)

print("Fixed Compilation Errors")
