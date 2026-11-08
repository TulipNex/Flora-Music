with open('app/src/main/java/com/example/player/MusicService.kt', 'r') as f:
    content = f.read()

target = """                override fun onPause() {
                    controller.togglePlay()
                }"""
replacement = """                override fun onPause() {
                    controller.togglePlay()
                }

                override fun onStop() {
                    if (controller.isPlaying.value) {
                        controller.togglePlay()
                    }
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
        f.write(content)
    print("Patched MusicService stop")
