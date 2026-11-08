package com.example.lyrics

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LyricWord(
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long
)

@JsonClass(generateAdapter = true)
data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val words: List<LyricWord> = emptyList(),
    val isBackground: Boolean = false,
    val agent: String? = null
)

sealed class LyricState {
    object Loading : LyricState()
    object None : LyricState()
    data class Static(val text: String) : LyricState()
    data class Synced(val lines: List<LyricLine>) : LyricState()
}
