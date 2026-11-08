package com.example.database

import androidx.room.*
import com.example.domain.model.Playlist
import com.example.domain.model.PlaylistSongMap
import com.example.domain.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsDirectly(): List<Song>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE lastPlayedTime > 0 ORDER BY lastPlayedTime DESC LIMIT 50")
    fun getRecentlyPlayedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT 50")
    fun getMostPlayedSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Update
    suspend fun updateSongs(songs: List<Song>)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsById(ids: List<Long>)

    @Query("UPDATE songs SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFav: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTime = :time WHERE id = :id")
    suspend fun incrementPlayCount(id: Long, time: Long)

    @Query("DELETE FROM songs")
    suspend fun clearAll()

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchSongs(query: String): Flow<List<Song>>
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(map: PlaylistSongMap)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs p ON s.id = p.songId
        WHERE p.playlistId = :playlistId
        ORDER BY s.title ASC
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>
}
