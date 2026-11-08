package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.FloraApplication
import com.example.utils.ArtworkCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ArtworkPreExtractorService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "ArtworkPreExtractorService onStartCommand.")
        
        serviceScope.launch {
            try {
                val app = application as FloraApplication
                val songs = app.repository.getAllSongsDirectly()
                Log.i(TAG, "Starting artwork pre-extraction for ${songs.size} songs...")
                
                var successCount = 0
                for (song in songs) {
                    val success = ArtworkCacheManager.cacheArtwork(app, song.path)
                    if (success) {
                        successCount++
                    }
                }
                Log.i(TAG, "Artwork pre-extraction completed. Successfully cached $successCount files.")
            } catch (e: Exception) {
                Log.e(TAG, "Error in ArtworkPreExtractorService", e)
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.i(TAG, "ArtworkPreExtractorService destroyed.")
    }

    companion object {
        private const val TAG = "ArtworkPreExtractor"

        fun start(context: Context) {
            try {
                val intent = Intent(context, ArtworkPreExtractorService::class.java)
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start ArtworkPreExtractorService", e)
            }
        }
    }
}
