package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.database.MusicDatabase
import com.example.player.MusicPlayerController
import com.example.player.MusicService
import com.example.repository.MusicRepository
import com.example.utils.AudioArtworkFetcher

class FloraApplication : Application(), ImageLoaderFactory {
    
    lateinit var repository: MusicRepository
    lateinit var playerController: MusicPlayerController

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        createNotificationChannel()

        // Initialize Room Local Database
        val database = MusicDatabase.getDatabase(this)
        
        // Initialize Repositories
        repository = MusicRepository(database.songDao(), database.playlistDao())
        
        // Initialize Media Player Coordinator
        playerController = MusicPlayerController(this, repository)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MusicService.CHANNEL_ID,
                "Flora Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback notification"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(AudioArtworkFetcher.Factory())
            }
            .crossfade(true)
            .build()
    }

    companion object {
        val appStartTime = System.currentTimeMillis()
        lateinit var instance: FloraApplication
            private set
    }
}
