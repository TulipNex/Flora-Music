package com.example.domain.model

import com.example.lyrics.LyricLine

data class LyricsData(
    val version: String,
    val provider: String,
    val downloadedAt: Long,
    val synced: Boolean,
    val sourceType: String,
    val language: String?,
    val lyrics: List<LyricLine>
)
