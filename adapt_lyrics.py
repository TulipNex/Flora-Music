import re

with open('temp_lyricsline.kt', 'r') as f:
    content = f.read()

# Extract from WordLevelLyrics to the end
idx = content.find('@Composable\nprivate fun WordLevelLyrics')
if idx == -1:
    print("WordLevelLyrics not found")
    exit(1)

word_level = content[idx:]
word_level = word_level.replace('private fun WordLevelLyrics', 'fun FloraWordLevelLyrics')
word_level = word_level.replace('words: List<WordTimestamp>,', 'words: List<com.example.lyrics.LyricWord>,')
word_level = word_level.replace('currentPositionState: Long,', 'playbackPosition: Long,\n    isPlaying: Boolean,')
word_level = word_level.replace('lyricsOffset: Long,', '')
word_level = word_level.replace('playerConnection: PlayerConnection,', '')

# Replace smoothPosition logic
smooth_pos_logic = """
    var smoothPosition by remember { mutableLongStateOf(playbackPosition) }
    
    LaunchedEffect(isActiveLine, isPlaying) {
        if (isActiveLine && isPlaying) {
            var lastUpdateTime = System.currentTimeMillis()
            var lastPos = playbackPosition
            while (isActive) {
                withFrameMillis {
                    val now = System.currentTimeMillis()
                    if (playbackPosition != lastPos) {
                        lastPos = playbackPosition
                        lastUpdateTime = now
                    }
                    val elapsed = now - lastUpdateTime
                    smoothPosition = lastPos + elapsed
                }
            }
        }
    }
    
    LaunchedEffect(isActiveLine, playbackPosition, isPlaying) {
        if (!isActiveLine || !isPlaying) {
            smoothPosition = playbackPosition
        }
    }
"""
# We need to replace the existing smoothPosition block
start_smooth = word_level.find('var smoothPosition')
end_smooth = word_level.find('val (effectiveWords, effectiveToOriginalIdx)')
word_level = word_level[:start_smooth] + smooth_pos_logic + "\n    " + word_level[end_smooth:]

# Replace WordTimestamp with LyricWord
word_level = word_level.replace('WordTimestamp(', 'com.example.lyrics.LyricWord(')
word_level = word_level.replace('word.startTime * 1000', 'word.startTimeMs')
word_level = word_level.replace('word.endTime * 1000', 'word.endTimeMs')
word_level = word_level.replace('word.startTime', 'word.startTimeMs')
word_level = word_level.replace('word.endTime', 'word.endTimeMs')

# WordTimestamp has startTime and endTime in seconds. Our LyricWord has it in ms.
# In the segment creation:
# startTime = word.startTime + index * segmentDuration
# we need to make sure this operates on ms.
word_level = word_level.replace('word.hasTrailingSpace', 'true') # mock it
word_level = word_level.replace('hasTrailingSpace = if (index == segments.size - 1) true else false', '')

# There might be some issues with WordTimestamp vs LyricWord constructors. Let's fix it by regex:
# com.example.lyrics.LyricWord( text = ..., startTimeMs = ..., endTimeMs = ... )
word_level = re.sub(r'com.example.lyrics.LyricWord\(\s*text = (.*?),\s*startTimeMs = (.*?),\s*endTimeMs = (.*?),\s*hasTrailingSpace = .*?\s*\)',
                    r'com.example.lyrics.LyricWord(text = \1, startTimeMs = (\2).toLong(), endTimeMs = (\3).toLong())', word_level)

word_level = re.sub(r'com.example.lyrics.LyricWord\(\s*text = (.*?),\s*startTimeMs = (.*?),\s*endTimeMs = (.*?)\s*\)',
                    r'com.example.lyrics.LyricWord(text = \1, startTimeMs = (\2).toLong(), endTimeMs = (\3).toLong())', word_level)


# Extract HyphenGroupWord and other helpers
helpers = """
private data class HyphenGroupWord(
    val pos: Int,
    val size: Int,
    val isLast: Boolean,
    val groupStartMs: Long,
    val groupEndMs: Long
)

private fun String.containsRtl(): Boolean {
    for (c in this) {
        val directionality = Character.getDirectionality(c).toInt()
        if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT.toInt() ||
            directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC.toInt()
        ) {
            return true
        }
    }
    return false
}

private fun String.toGraphemeClusters(): List<String> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<String>()
    val it = java.text.BreakIterator.getCharacterInstance()
    it.setText(this)
    var start = it.first()
    var end = it.next()
    while (end != java.text.BreakIterator.DONE) {
        result.add(substring(start, end))
        start = end
        end = it.next()
    }
    return result
}
"""

with open('FloraWordLevelLyrics.kt', 'w') as f:
    f.write(helpers + "\n" + word_level)

