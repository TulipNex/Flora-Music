package com.example.repository

import android.content.Context
import com.example.database.PlaylistDao
import com.example.database.SongDao
import com.example.domain.model.Playlist
import com.example.domain.model.PlaylistSongMap
import com.example.domain.model.Song
import com.example.utils.MusicScanner
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {
    // Song standard flows
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()
    val recentlyPlayedSongs: Flow<List<Song>> = songDao.getRecentlyPlayedSongs()
    val mostPlayedSongs: Flow<List<Song>> = songDao.getMostPlayedSongs()

    // Database access helpers
    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)
    suspend fun getAllSongsDirectly(): List<Song> = songDao.getAllSongsDirectly()

    suspend fun updateSong(song: Song) = songDao.updateSong(song)

    suspend fun setSongFavorite(id: Long, isFav: Boolean) = songDao.updateFavoriteStatus(id, isFav)

    suspend fun markSongPlayed(id: Long) {
        val now = System.currentTimeMillis()
        songDao.incrementPlayCount(id, now)
    }

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    // Scanning action - Highly optimized incremental synchronize
    suspend fun scanLocalLibrary(context: Context, onProgress: (scanned: Int, total: Int) -> Unit) {
        val syncStartTime = System.currentTimeMillis()
        android.util.Log.i("FLORA_SYNC", "Starting background smart incremental synchronization...")

        // 1. Fetch current songs from database
        val dbStartTime = System.currentTimeMillis()
        val existingSongs = songDao.getAllSongsDirectly()
        val dbLoadTime = System.currentTimeMillis() - dbStartTime
        android.util.Log.i("FLORA_SYNC", "Loaded ${existingSongs.size} cached songs from database in ${dbLoadTime}ms")

        val existingSongsMap = existingSongs.associateBy { it.id }

        // 2. Query MediaStore for raw files details
        val mediaStoreSongs = MusicScanner.queryRawSongsFromMediaStore(context)
        android.util.Log.i("FLORA_SYNC", "Fetched ${mediaStoreSongs.size} raw files from MediaStore")

        val mediaStoreSongsMap = mediaStoreSongs.associateBy { it.id }

        // 3. Compare lists to identify actions
        val songsToInsert = mutableListOf<Song>()
        val songsToUpdate = mutableListOf<Song>()
        val idsToDelete = mutableListOf<Long>()

        // Find missing files (Deleted from MediaStore or no longer exist)
        for ((id, cachedSong) in existingSongsMap) {
            val mediaStoreSong = mediaStoreSongsMap[id]
            if (mediaStoreSong == null || !java.io.File(cachedSong.path).exists()) {
                idsToDelete.add(id)
            }
        }

        // Find added or updated files
        val itemsToProcess = mutableListOf<Pair<Song, Song?>>() // Pair of (rawMediaStoreSong, existingCachedSongOrNull)
        for (rawSong in mediaStoreSongs) {
            val cachedSong = existingSongsMap[rawSong.id]
            if (cachedSong == null) {
                // New file
                itemsToProcess.add(Pair(rawSong, null))
            } else {
                // Check if file metadata or details changed
                val hasChanged = cachedSong.path != rawSong.path || 
                                 cachedSong.size != rawSong.size || 
                                 cachedSong.dateAdded != rawSong.dateAdded || 
                                 cachedSong.duration != rawSong.duration ||
                                 cachedSong.dateModified != rawSong.dateModified

                if (hasChanged) {
                    itemsToProcess.add(Pair(rawSong, cachedSong))
                }
            }
        }

        val totalToProcess = itemsToProcess.size
        android.util.Log.i("FLORA_SYNC", "Sync Plan: Run incremental enrichment for $totalToProcess items, delete ${idsToDelete.size} items.")

        // 4. Enrich only those that need processing
        for (index in itemsToProcess.indices) {
            val (rawSong, cachedSong) = itemsToProcess[index]
            try {
                val enriched = MusicScanner.enrichMetadata(rawSong)
                if (cachedSong == null) {
                    songsToInsert.add(enriched)
                } else {
                    // Retain user custom fields or flags
                    val updatedSong = enriched.copy(
                        isFavorite = cachedSong.isFavorite,
                        playCount = cachedSong.playCount,
                        lastPlayedTime = cachedSong.lastPlayedTime,
                        customCoverUri = cachedSong.customCoverUri
                    )
                    songsToUpdate.add(updatedSong)
                }
            } catch (e: Exception) {
                android.util.Log.e("FLORA_SYNC", "Error enriching metadata for: ${rawSong.path}", e)
            }
            onProgress(index + 1, totalToProcess)
        }

        // 5. Apply the modifications to local database in batch
        if (songsToInsert.isNotEmpty()) {
            songDao.insertAll(songsToInsert)
        }
        if (songsToUpdate.isNotEmpty()) {
            songDao.updateSongs(songsToUpdate)
        }
        if (idsToDelete.isNotEmpty()) {
            songDao.deleteSongsById(idsToDelete)
        }

        val syncDuration = System.currentTimeMillis() - syncStartTime
        val startupDuration = System.currentTimeMillis() - com.example.FloraApplication.appStartTime
        android.util.Log.i("FLORA_SYNC", "--- INCREMENTAL SYNC SUMMARY ---")
        android.util.Log.i("FLORA_SYNC", "Background sync completed in ${syncDuration}ms")
        android.util.Log.i("FLORA_SYNC", "- Startup time: ${startupDuration}ms")
        android.util.Log.i("FLORA_SYNC", "- Cache load time: ${dbLoadTime}ms")
        android.util.Log.i("FLORA_SYNC", "- Total scanned: ${mediaStoreSongs.size} files")
        android.util.Log.i("FLORA_SYNC", "- Added: ${songsToInsert.size} new songs")
        android.util.Log.i("FLORA_SYNC", "- Updated: ${songsToUpdate.size} songs with changed metadata/file details")
        android.util.Log.i("FLORA_SYNC", "- Removed: ${idsToDelete.size} missing songs")
        android.util.Log.i("FLORA_SYNC", "--------------------------------")
    }

    suspend fun clearLibrary() {
        songDao.clearAll()
    }

    // Playlist standard operations
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongMap(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> = playlistDao.getSongsInPlaylist(playlistId)
}
