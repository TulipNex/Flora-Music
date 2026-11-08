import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target = """    val coverPath = preferredCoverPath ?: songs.firstOrNull()?.path"""

replacement = """    val firstSong = songs.firstOrNull()
    val coverPath = preferredCoverPath ?: firstSong?.path
    val coverUri = firstSong?.customCoverUri"""

content = content.replace(target, replacement)

target2 = """.data(coverPath?.let { AudioArtwork(it) })"""
replacement2 = """.data(coverPath?.let { AudioArtwork(it, coverUri) })"""
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
