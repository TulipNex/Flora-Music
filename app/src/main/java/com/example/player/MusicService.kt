package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.example.FloraApplication
import com.example.MainActivity
import com.example.domain.model.Song
import com.example.utils.ArtworkCacheManager
import com.example.utils.MusicScanner
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class MusicService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val app = application as FloraApplication
        val player = app.playerController.player

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setNotificationId(NOTIFICATION_ID)
            .build()
        setMediaNotificationProvider(notificationProvider)

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
                return Futures.immediateFuture(
                    LibraryResult.ofItem(
                        MediaItem.Builder().setMediaId("root").build(), params
                    )
                )
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

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .setId("FloraMusicService")
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        // Observe playback state and current song to automatically update rich notification
        serviceScope.launch {
            combine(
                app.playerController.currentSong,
                app.playerController.isPlaying
            ) { song, isPlaying ->
                Pair(song, isPlaying)
            }.collect { (song, isPlaying) ->
                updateNotification(song, isPlaying)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as FloraApplication
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                app.playerController.togglePlay()
            }
            ACTION_NEXT -> {
                app.playerController.next()
            }
            ACTION_PREVIOUS -> {
                app.playerController.previous()
            }
            ACTION_FAVORITE -> {
                val currentSong = app.playerController.currentSong.value
                if (currentSong != null) {
                    val newFav = !currentSong.isFavorite
                    currentSong.isFavorite = newFav
                    serviceScope.launch {
                        app.repository.setSongFavorite(currentSong.id, newFav)
                        updateNotification(currentSong, app.playerController.isPlaying.value)
                    }
                }
            }
            ACTION_STOP -> {
                if (app.playerController.isPlaying.value) {
                    app.playerController.togglePlay()
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val currentSong = app.playerController.currentSong.value
        val isPlaying = app.playerController.isPlaying.value
        updateNotification(currentSong, isPlaying)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateNotification(song: Song?, isPlaying: Boolean) {
        try {
            val notification = buildMediaNotification(song, isPlaying)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            if (!isPlaying) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(false)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicService", "Error updating notification", e)
        }
    }

    private fun buildMediaNotification(song: Song?, isPlaying: Boolean): Notification {
        createNotificationChannel()

        val title = song?.title ?: "Flora Music"
        val artist = song?.artist ?: "Pemutar Musik"
        val album = song?.album ?: ""

        val favIntent = createPendingIntent(ACTION_FAVORITE, 1)
        val prevIntent = createPendingIntent(ACTION_PREVIOUS, 2)
        val playPauseIntent = createPendingIntent(ACTION_PLAY_PAUSE, 3)
        val nextIntent = createPendingIntent(ACTION_NEXT, 4)
        val stopIntent = createPendingIntent(ACTION_STOP, 5)

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val artworkBitmap = loadArtworkBitmap(song)

        val isFav = song?.isFavorite == true
        val favIcon = if (isFav) com.example.R.drawable.ic_favorite else com.example.R.drawable.ic_favorite_border
        val favTitle = if (isFav) "Unlike" else "Like"

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(1, 2, 3) // Previous (1), Play/Pause (2), Next (3)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopIntent)

        mediaLibrarySession?.sessionCompatToken?.let { token ->
            mediaStyle.setMediaSession(token)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(if (isPlaying) com.example.R.drawable.ic_play_arrow else com.example.R.drawable.ic_pause)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(album.ifEmpty { null })
            .setLargeIcon(artworkBitmap)
            .setContentIntent(contentIntent)
            .setStyle(mediaStyle)
            .setColorized(true)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(favIcon, favTitle, favIntent)
            .addAction(com.example.R.drawable.ic_skip_previous, "Previous", prevIntent)
            .addAction(
                if (isPlaying) com.example.R.drawable.ic_pause else com.example.R.drawable.ic_play_arrow,
                if (isPlaying) "Pause" else "Play",
                playPauseIntent
            )
            .addAction(com.example.R.drawable.ic_skip_next, "Next", nextIntent)
            .addAction(com.example.R.drawable.ic_dismiss, "Stop", stopIntent)

        return builder.build()
    }

    private fun loadArtworkBitmap(song: Song?): Bitmap? {
        if (song == null) return null
        return try {
            if (!song.customCoverUri.isNullOrEmpty()) {
                val uri = Uri.parse(song.customCoverUri)
                contentResolver.openInputStream(uri)?.use { stream ->
                    decodeSampledBitmap(stream.readBytes())
                }
            } else {
                val cachedFile = ArtworkCacheManager.getCachedFile(this, song.path)
                if (cachedFile != null && cachedFile.exists()) {
                    decodeSampledBitmap(cachedFile.absolutePath)
                } else {
                    val bytes = MusicScanner.getArtworkBytes(song.path)
                    if (bytes != null) {
                        decodeSampledBitmap(bytes)
                    } else null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeSampledBitmap(data: ByteArray, reqWidth: Int = 512, reqHeight: Int = 512): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeSampledBitmap(path: String, reqWidth: Int = 512, reqHeight: Int = 512): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun createPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, requestCode, intent, flags)
        } else {
            PendingIntent.getService(this, requestCode, intent, flags)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Flora Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls and notifications"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
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

            app.playerController.syncQueueFromExternal(targetSongs, 0)

            targetSongs.map { s ->
                val metadataBuilder = MediaMetadata.Builder()
                    .setTitle(s.title)
                    .setArtist(s.artist)
                    .setAlbumTitle(s.album)
                    .setIsPlayable(true)

                if (!s.customCoverUri.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(s.customCoverUri)
                        if (s.customCoverUri.startsWith("content://")) {
                            metadataBuilder.setArtworkUri(uri)
                        } else {
                            val file = File(uri.path ?: s.customCoverUri)
                            if (file.exists()) {
                                metadataBuilder.setArtworkData(file.readBytes(), MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                            }
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                } else {
                    val cachedFile = ArtworkCacheManager.getCachedFile(this, s.path)
                    if (cachedFile != null && cachedFile.exists()) {
                        try {
                            metadataBuilder.setArtworkData(cachedFile.readBytes(), MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }

                val metadata = metadataBuilder.build()
                MediaItem.Builder()
                    .setMediaId(s.id.toString())
                    .setUri(Uri.fromFile(File(s.path)))
                    .setMediaMetadata(metadata)
                    .build()
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val app = application as? FloraApplication
        val isPlaying = app?.playerController?.isPlaying?.value == true
        if (!isPlaying) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            release()
            mediaLibrarySession = null
        }
        serviceJob.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "flora_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY_PAUSE = "com.example.flora.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.flora.NEXT"
        const val ACTION_PREVIOUS = "com.example.flora.PREVIOUS"
        const val ACTION_FAVORITE = "com.example.flora.FAVORITE"
        const val ACTION_STOP = "com.example.flora.STOP"
    }
}
