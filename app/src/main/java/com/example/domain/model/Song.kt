package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val genre: String = "Unknown",
    val albumArtist: String = "Unknown",
    val composer: String = "Unknown",
    val writer: String = "",
    val discNumber: String = "",
    val comment: String = "",
    val customCoverUri: String? = null,
    var isFavorite: Boolean = false,
    var playCount: Int = 0,
    var lastPlayedTime: Long = 0L,
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val lyricsEmbedded: String? = null,
    val lyricsSynced: String? = null
)
