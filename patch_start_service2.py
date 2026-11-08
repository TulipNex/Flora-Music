with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'r') as f:
    content = f.read()

import re
# Remove the `.apply { action = ... }` part
content = re.sub(r'val intent = Intent\(context, MusicService::class\.java\)\.apply \{\s*action = MusicService\.ACTION_START_FOREGROUND\s*\}', 'val intent = Intent(context, MusicService::class.java)', content)

with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'w') as f:
    f.write(content)
print("Patched startPlaybackService 2")
