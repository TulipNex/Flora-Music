package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.database.LyricsDao
import com.example.database.LyricsEntity
import com.example.domain.model.LyricsData
import com.example.domain.model.Song
import com.example.domain.provider.BetterLyricsProvider
import com.example.domain.provider.LyricsProvider
import com.example.lyrics.LyricState
import com.example.lyrics.LyricsParser
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LyricsRepository(
    private val context: Context,
    private val lyricsDao: LyricsDao
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    
    // We can add multiple providers here in the future
    private val providers: List<LyricsProvider> = listOf(
        BetterLyricsProvider()
    )

    suspend fun getLyrics(song: Song): LyricsData? = withContext(Dispatchers.IO) {
        try {
            // STEP 1 & 2: Check local database cache first
            val cachedEntity = try { lyricsDao.getLyricsByTrackId(song.id) } catch (e: Exception) { null }
            if (cachedEntity != null) {
                return@withContext cachedEntity.toDomainModel(moshi)
            }

            // STEP 3: If not in cache, check internet connection
            if (isInternetAvailable(context)) {
                // STEP 4: Online - Fetch from providers
                for (provider in providers) {
                    try {
                        val fetchedData = provider.fetchLyrics(song)
                        if (fetchedData != null) {
                            // Save to local database
                            try {
                                val entity = LyricsEntity.fromDomainModel(moshi, song.id, song, fetchedData)
                                lyricsDao.insertLyrics(entity)
                            } catch (e: Exception) {
                                // Ignore caching failure
                            }
                            return@withContext fetchedData
                        }
                    } catch (e: Exception) {
                        // Provider failed, try next
                    }
                }
            }

            // STEP 4 & 5: If offline or provider failed/returned null, fallback to embedded lyrics
            val rawEmbedded = song.lyricsSynced ?: song.lyricsEmbedded
            if (!rawEmbedded.isNullOrBlank()) {
                val parsedState = LyricsParser.parse(rawEmbedded)
                val lines = when (parsedState) {
                    is LyricState.Synced -> parsedState.lines
                    is LyricState.Static -> listOf(com.example.lyrics.LyricLine(0, parsedState.text))
                    else -> emptyList()
                }
                
                if (lines.isNotEmpty()) {
                    // Do not save embedded lyrics to database, just return as LyricsData
                    return@withContext LyricsData(
                        version = "1.0",
                        provider = "embedded",
                        downloadedAt = System.currentTimeMillis(),
                        synced = parsedState is LyricState.Synced,
                        sourceType = "embedded",
                        language = null,
                        lyrics = lines
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LyricsRepository", "Error getting lyrics for ${song.title}", e)
        }

        // Target: "Lirik tidak tersedia" will be handled by UI if this returns null
        return@withContext null
    }

    private fun isInternetAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            false
        }
    }
}
