package com.example.domain.provider

import com.example.domain.model.LyricsData
import com.example.domain.model.Song
import com.example.lyrics.LyricState
import com.example.lyrics.LyricsParser
import com.example.lyrics.TTMLParser
import com.example.network.BetterLyricsApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class BetterLyricsProvider : LyricsProvider {
    override val providerName: String = "betterlyrics"

    private val api: BetterLyricsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://lyrics-api.boidu.dev/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(BetterLyricsApi::class.java)
    }

    override suspend fun fetchLyrics(song: Song): LyricsData? {
        try {
            val durationSec = if (song.duration > 0) (song.duration / 1000).toInt() else null
            val response = api.searchLyrics(
                title = song.title,
                artist = song.artist,
                album = song.album,
                durationSec = durationSec
            )
            
            if (response.isSuccessful) {
                val body = response.body() ?: return null
                
                val rawLyrics = body.ttml?.trim()?.takeIf { it.isNotEmpty() } ?: return null
                
                val parsedLines = TTMLParser.parseTTML(rawLyrics)
                if (parsedLines.isEmpty()) return null
                
                val lrcString = TTMLParser.toLRC(parsedLines)
                
                val parsedState = LyricsParser.parse(lrcString)
                val lines = when (parsedState) {
                    is LyricState.Synced -> parsedState.lines
                    is LyricState.Static -> listOf(com.example.lyrics.LyricLine(0, parsedState.text))
                    else -> emptyList()
                }
                
                if (lines.isEmpty()) return null
                
                return LyricsData(
                    version = "1.0",
                    provider = providerName,
                    downloadedAt = System.currentTimeMillis(),
                    synced = parsedState is LyricState.Synced,
                    sourceType = providerName,
                    language = null,
                    lyrics = lines
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
