package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics WHERE trackId = :trackId")
    suspend fun getLyricsByTrackId(trackId: Long): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)
    
    @Query("DELETE FROM lyrics WHERE trackId = :trackId")
    suspend fun deleteLyrics(trackId: Long)
    
    @Query("DELETE FROM lyrics")
    suspend fun clearAll()
}
