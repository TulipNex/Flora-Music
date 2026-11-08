package com.example.domain.provider

import com.example.domain.model.LyricsData
import com.example.domain.model.Song

interface LyricsProvider {
    val providerName: String
    suspend fun fetchLyrics(song: Song): LyricsData?
}
