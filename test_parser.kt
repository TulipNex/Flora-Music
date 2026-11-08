import java.util.regex.Pattern

data class LyricLine(val timestampMs: Long, val text: String, val isBackground: Boolean = false, val agent: String? = null)

object LyricsParser {
    private val TIMESTAMP_PATTERN: Pattern = Pattern.compile("\\[(\\d{2,}):(\\d{2})(?:[.:](\\d{2,3}))?\\]")

    fun parse(lyricsText: String?): List<LyricLine> {
        if (lyricsText.isNullOrBlank()) return emptyList()

        val lines = lyricsText.split("\n", "\r\n")
        val parsedLines = mutableListOf<LyricLine>()
        var hasTimestamp = false
        var currentBg = false
        var currentAgent: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") || 
                trimmed.startsWith("[al:") || trimmed.startsWith("[by:") || 
                trimmed.startsWith("[offset:")) {
                continue
            }

            val matcher = TIMESTAMP_PATTERN.matcher(trimmed)
            val timestamps = mutableListOf<Long>()
            var lastMatchEnd = 0

            while (matcher.find()) {
                val minStr = matcher.group(1) ?: "0"
                val secStr = matcher.group(2) ?: "0"
                val msStr = matcher.group(3) ?: "0"

                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L
                    
                val msVal = msStr.toLongOrNull() ?: 0L
                val ms = if (msStr.length == 2) msVal * 10L else msVal

                val totalMs = (minutes * 60 + seconds) * 1000L + ms
                timestamps.add(totalMs)
                lastMatchEnd = matcher.end()
            }

            if (timestamps.isNotEmpty()) {
                hasTimestamp = true
                var lyricText = trimmed.substring(lastMatchEnd).trim()
                    
                if (lyricText.contains("{bg}")) {
                    currentBg = true
                    lyricText = lyricText.replace("{bg}", "")
                } else if (lyricText.contains("{agent:")) {
                    currentBg = false
                    val agentRegex = Regex("\\\\{agent:([^}]+)\\\\}")
                    val agentMatch = agentRegex.find(lyricText)
                    if (agentMatch != null) {
                        currentAgent = agentMatch.groupValues[1]
                        lyricText = lyricText.replace(agentMatch.value, "")
                    }
                }

                lyricText = lyricText.trim()

                for (time in timestamps) {
                    parsedLines.add(LyricLine(time, lyricText, isBackground = currentBg, agent = currentAgent))
                }
            }
        }

        return parsedLines.sortedBy { it.timestampMs }
    }
}

fun main() {
    val lyrics = """
[00:10.00] Line 1
[00:15.00] {bg} Line 2 (bg)
[00:20.00] Line 3
[00:25.00] {agent:Miku} Line 4 (agent)
[00:30.00] Line 5
    """.trimIndent()
    val parsed = LyricsParser.parse(lyrics)
    parsed.forEach { println("${it.timestampMs}: ${it.text} (bg=${it.isBackground})") }
}
