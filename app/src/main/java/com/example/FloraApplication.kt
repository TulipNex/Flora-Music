package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.database.MusicDatabase
import com.example.player.MusicPlayerController
import com.example.repository.MusicRepository
import com.example.utils.AudioArtworkFetcher

class FloraApplication : Application(), ImageLoaderFactory {
    
    lateinit var repository: MusicRepository
    lateinit var playerController: MusicPlayerController

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Room Local Database
        val database = MusicDatabase.getDatabase(this)
        
        // Initialize Repositories
        repository = MusicRepository(database.songDao(), database.playlistDao())
        
        // Initialize Media Player Coordinator
        playerController = MusicPlayerController(this, repository)
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
