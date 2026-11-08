with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'r') as f:
    content = f.read()

new_method = """    fun syncQueueFromExternal(playQueue: List<Song>, startIndex: Int) {
        _queue.value = playQueue
        _currentIndex.value = startIndex
        _currentSong.value = playQueue.getOrNull(startIndex)
    }
"""

if 'fun syncQueueFromExternal' not in content:
    content = content.replace('fun play(song: Song, playQueue: List<Song>) {', new_method + '\n    fun play(song: Song, playQueue: List<Song>) {')
    with open('app/src/main/java/com/example/player/MusicPlayerController.kt', 'w') as f:
        f.write(content)
    print("Patched syncQueueFromExternal")
