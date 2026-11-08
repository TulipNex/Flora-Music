package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.LyricsData
import com.example.domain.model.Song
import com.example.lyrics.LyricLine
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey val trackId: Long,
    val title: String,
    val artist: String,
    val album: String?,
    val duration: Long,
    val provider: String,
    val downloadedAt: Long,
    val sourceType: String,
    val language: String?,
    val version: String,
    val lyricsJson: String 
) {
    fun toDomainModel(moshi: Moshi): LyricsData {
        val type = Types.newParameterizedType(List::class.java, LyricLine::class.java)
        val adapter = moshi.adapter<List<LyricLine>>(type)
        val lyricsList = try {
            adapter.fromJson(lyricsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        
        return LyricsData(
            version = version,
            provider = provider,
            downloadedAt = downloadedAt,
            synced = lyricsList.any { it.timestampMs > 0 } || sourceType != "embedded_static",
            sourceType = sourceType,
            language = language,
            lyrics = lyricsList
        )
    }

    companion object {
        fun fromDomainModel(moshi: Moshi, trackId: Long, song: Song, data: LyricsData): LyricsEntity {
            val type = Types.newParameterizedType(List::class.java, LyricLine::class.java)
            val adapter = moshi.adapter<List<LyricLine>>(type)
            val json = try {
                adapter.toJson(data.lyrics)
            } catch (e: Exception) {
                "[]"
            }
            
            return LyricsEntity(
                trackId = trackId,
                title = song.title,
                artist = song.artist,
                album = song.album,
                duration = song.duration,
                provider = data.provider,
                downloadedAt = data.downloadedAt,
                sourceType = data.sourceType,
                language = data.language,
                version = data.version,
                lyricsJson = json
            )
        }
    }
}
