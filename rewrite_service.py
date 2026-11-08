code = """package com.example.player

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.LibraryResult
import com.example.FloraApplication
import com.example.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.Futures
import com.google.common.collect.ImmutableList
import android.net.Uri

class MusicService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        val app = application as FloraApplication
        val player = app.playerController.player

        val callback = object : MediaLibrarySession.Callback {
            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                val updatedMediaItems = mutableListOf<MediaItem>()
                
                for (item in mediaItems) {
                    val query = item.requestMetadata.searchQuery
                    if (query != null) {
                        val resultItems = handleSearchBlocking(query)
                        updatedMediaItems.addAll(resultItems)
                    } else {
                        updatedMediaItems.add(item)
                    }
                }
                return Futures.immediateFuture(updatedMediaItems)
            }
            
            override fun onGetLibraryRoot(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<MediaItem>> {
                return Futures.immediateFuture(LibraryResult.ofItem(
                    MediaItem.Builder().setMediaId("root").build(), params
                ))
            }

            override fun onGetChildren(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                parentId: String,
                page: Int,
                pageSize: Int,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
                return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of(), params))
            }

            override fun onSearch(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                query: String,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<Void>> {
                session.notifySearchResultChanged(browser, query, 0, params)
                return Futures.immediateFuture(LibraryResult.ofVoid())
            }

            override fun onGetSearchResult(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                query: String,
                page: Int,
                pageSize: Int,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
                return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of(), params))
            }
        }

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .setId("FloraMusicService")
            .build()
    }
    
    private fun handleSearchBlocking(query: String): List<MediaItem> {
        return runBlocking {
            val app = application as FloraApplication
            val repo = app.repository
            val songs = repo.allSongs.firstOrNull() ?: emptyList()
            if (songs.isEmpty()) return@runBlocking emptyList()

            val matchedSongs = songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
            
            val targetSongs = if (matchedSongs.isNotEmpty()) matchedSongs else listOf(songs.first())
            
            // Sync with our controller's state
            app.playerController.syncQueueFromExternal(targetSongs, 0)
            
            targetSongs.map { s ->
                val metadata = MediaMetadata.Builder()
                    .setTitle(s.title)
                    .setArtist(s.artist)
                    .setAlbumTitle(s.album)
                    .setArtworkUri(if (!s.customCoverUri.isNullOrEmpty()) Uri.parse(s.customCoverUri) else null)
                    .setIsPlayable(true)
                    .build()
                MediaItem.Builder()
                    .setMediaId(s.id.toString())
                    .setUri(Uri.fromFile(java.io.File(s.path)))
                    .setMediaMetadata(metadata)
                    .build()
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        serviceJob.cancel()
        super.onDestroy()
    }
}
"""
with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
    f.write(code)
print("Rewrote MusicService.kt")
