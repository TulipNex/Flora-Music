package com.example.lyrics

import java.util.regex.Pattern

object LyricsParser {
    // Regex to match timestamp patterns like [00:12.34] or [01:02:03] or [00:12]
    private val TIMESTAMP_PATTERN: Pattern = Pattern.compile("\\[(\\d{2,}):(\\d{2})(?:[.:](\\d{2,3}))?\\]")

    fun parse(lyricsText: String?): LyricState {
        if (lyricsText.isNullOrBlank()) return LyricState.None

        val lines = lyricsText.split("\n", "\r\n")
        val parsedLines = mutableListOf<LyricLine>()
        var hasTimestamp = false
        
        var currentAgent: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Skip common metadata headers in LRC files like [ti:Title] [ar:Artist]
            if (trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") || 
                trimmed.startsWith("[al:") || trimmed.startsWith("[by:") || 
                trimmed.startsWith("[offset:")) {
                continue
            }

            if (trimmed.startsWith("<") && trimmed.endsWith(">") && parsedLines.isNotEmpty()) {
                val wordsContent = trimmed.substring(1, trimmed.length - 1)
                val wordTokens = wordsContent.split("|")
                val parsedWords = mutableListOf<LyricWord>()
                for (token in wordTokens) {
                    val parts = token.split(":")
                    if (parts.size >= 3) {
                        val wordText = parts.dropLast(2).joinToString(":")
                        val startSec = parts[parts.size - 2].toDoubleOrNull() ?: 0.0
                        val endSec = parts[parts.size - 1].toDoubleOrNull() ?: 0.0
                        parsedWords.add(LyricWord(wordText, (startSec * 1000).toLong(), (endSec * 1000).toLong()))
                    }
                }
                if (parsedWords.isNotEmpty()) {
                    val lastLine = parsedLines.removeAt(parsedLines.lastIndex)
                    parsedLines.add(lastLine.copy(words = parsedWords))
                }
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
                
                // If ms is 2 digits (centisecond, common in LRC), multiply by 10. If 3 digits, do nothing.
                val msVal = msStr.toLongOrNull() ?: 0L
                val ms = if (msStr.length == 2) msVal * 10L else msVal

                val totalMs = (minutes * 60 + seconds) * 1000L + ms
                timestamps.add(totalMs)
                lastMatchEnd = matcher.end()
            }

            if (timestamps.isNotEmpty()) {
                hasTimestamp = true
                var lyricText = trimmed.substring(lastMatchEnd).trim()
                
                var currentBg = false
                if (lyricText.contains("{bg}")) {
                    currentBg = true
                    lyricText = lyricText.replace("{bg}", "")
                } else if (lyricText.contains("{agent:")) {
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

        return if (hasTimestamp && parsedLines.isNotEmpty()) {
            // Sort by time in case there were multiple timestamps on different lines out of order
            val sortedLines = parsedLines.sortedBy { it.timestampMs }
            LyricState.Synced(sortedLines)
        } else {
            // If no timestamps detected, treat as Static lyrics
            LyricState.Static(lyricsText)
        }
    }

    /**
     * Helper to retrieve currently highlighted lyric line based on track play time
     */
    fun getActiveLineIndex(lines: List<LyricLine>, currentPositionMs: Long): Int {
        if (lines.isEmpty()) return -1
        
        // Find the last lyric line whose timestamp is <= currentPositionMs
        var activeIndex = -1
        for (i in lines.indices) {
            if (lines[i].timestampMs <= currentPositionMs) {
                activeIndex = i
            } else {
                break
            }
        }
        return activeIndex
    }
}
