import re
with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'r') as f:
    content = f.read()

if 'fun playFromSearch' not in content:
    print("playFromSearch not in MusicPlayerController")
