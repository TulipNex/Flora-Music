import re

with open('app/src/main/java/com/example/domain/model/Song.kt', 'r') as f:
    content = f.read()

target = """    val albumArtist: String = "Unknown",
    val composer: String = "Unknown",
    var isFavorite: Boolean = false,"""

replacement = """    val albumArtist: String = "Unknown",
    val composer: String = "Unknown",
    val writer: String = "",
    val discNumber: String = "",
    val comment: String = "",
    val customCoverUri: String? = null,
    var isFavorite: Boolean = false,"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/domain/model/Song.kt', 'w') as f:
    f.write(content)
