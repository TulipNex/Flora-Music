package com.example.player

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.domain.model.Song
import com.example.repository.MusicRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayerController(
    private val context: Context,
    private val repository: MusicRepository
) {
    private val tag = "MusicPlayerController"
    
    val player: ExoPlayer get() = exoPlayer
    private var exoPlayer: ExoPlayer = run {
        val appContext = context.applicationContext
        androidx.media3.exoplayer.ExoPlayer.Builder(appContext).build().apply {
            val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(audioAttributes, true)
        }
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF) // 0=off, 1=one, 2=all
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _sleepTimerSecondsLeft = MutableStateFlow<Int?>(null)
    val sleepTimerSecondsLeft: StateFlow<Int?> = _sleepTimerSecondsLeft.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        setupPlayer()
        startPlaybackPositionTracker()
    }

    private fun setupPlayer() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _currentSong.value?.let { song ->
                        scope.launch {
                            repository.markSongPlayed(song.id)
                        }
                    }
                    startPlaybackService()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    if (_sleepTimerExpiredAndWaiting) {
                        _sleepTimerExpiredAndWaiting = false
                        _sleepTimerWaitUntilFinished = false
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && _sleepTimerExpiredAndWaiting) {
                    exoPlayer.pause()
                    _sleepTimerExpiredAndWaiting = false
                    _sleepTimerWaitUntilFinished = false
                    return
                }

                val index = exoPlayer.currentMediaItemIndex
                if (index in _queue.value.indices) {
                    _currentIndex.value = index
                    _currentSong.value = _queue.value[index]
                    _playbackPosition.value = 0L
                    _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                    startPlaybackService()
                }
            }
        })
    }

    private fun startPlaybackPositionTracker() {
        scope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    _playbackPosition.value = exoPlayer.currentPosition
                }
                delay(200)
            }
        }
    }

        fun syncQueueFromExternal(playQueue: List<Song>, startIndex: Int) {
        _queue.value = playQueue
        _currentIndex.value = startIndex
        _currentSong.value = playQueue.getOrNull(startIndex)
    }

    fun play(song: Song, playQueue: List<Song>) {
        val songIndex = playQueue.indexOfFirst { it.id == song.id }
        if (songIndex == -1) return

        _queue.value = playQueue
        _currentIndex.value = songIndex
        _currentSong.value = song

        val mediaItems = playQueue.map { s ->
            val metadata = androidx.media3.common.MediaMetadata.Builder()
                .setTitle(s.title)
                .setArtist(s.artist)
                .setAlbumTitle(s.album)
                .setArtworkUri(if (!s.customCoverUri.isNullOrEmpty()) Uri.parse(s.customCoverUri) else null)
                .build()
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(Uri.fromFile(java.io.File(s.path)))
                .setMediaMetadata(metadata)
                .build()
        }

        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.seekTo(songIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        
        // Sync repeat and shuffle settings with exoPlayer configuration
        exoPlayer.shuffleModeEnabled = _shuffleMode.value
        exoPlayer.repeatMode = _repeatMode.value
        exoPlayer.setPlaybackSpeed(_playbackSpeed.value)

        startPlaybackService()
    }

    fun togglePlay() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                exoPlayer.prepare()
            }
            exoPlayer.play()
            startPlaybackService()
        }
    }

    fun next() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL && _queue.value.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
        }
    }

    fun previous() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else {
            exoPlayer.seekTo(0L)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playbackPosition.value = positionMs
    }

    fun setShuffle(enabled: Boolean) {
        _shuffleMode.value = enabled
        exoPlayer.shuffleModeEnabled = enabled
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        exoPlayer.repeatMode = nextMode
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer.setPlaybackSpeed(speed)
    }

    private var _sleepTimerWaitUntilFinished = false
    private var _sleepTimerExpiredAndWaiting = false

    fun startSleepTimer(seconds: Int, waitUntilFinished: Boolean = false) {
        sleepTimerJob?.cancel()
        _sleepTimerExpiredAndWaiting = false
        _sleepTimerWaitUntilFinished = waitUntilFinished
        
        if (seconds <= 0) {
            _sleepTimerSecondsLeft.value = null
            return
        }

        _sleepTimerSecondsLeft.value = seconds
        sleepTimerJob = scope.launch {
            var secondsLeft = seconds
            while (secondsLeft > 0) {
                delay(1000L) // 1 second
                secondsLeft--
                _sleepTimerSecondsLeft.value = if (secondsLeft > 0) secondsLeft else null
            }
            if (_sleepTimerWaitUntilFinished) {
                _sleepTimerExpiredAndWaiting = true
            } else {
                exoPlayer.pause()
            }
            _sleepTimerSecondsLeft.value = null
        }
    }

    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerSecondsLeft.value = null
        _sleepTimerWaitUntilFinished = false
        _sleepTimerExpiredAndWaiting = false
    }

    fun updateCurrentSongReference(updatedSong: Song) {
        if (_currentSong.value?.id == updatedSong.id) {
            _currentSong.value = updatedSong
        }
    }

    private fun startPlaybackService() {
        try {
            val intent = Intent(context, MusicService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    context.startForegroundService(intent)
                } catch (e: Exception) {
                    context.startService(intent)
                }
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start MusicService", e)
        }
    }

    fun release() {
        scope.cancel()
        exoPlayer.release()
    }
}
