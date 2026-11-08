package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.database.MusicDatabase
import com.example.domain.model.Song
import com.example.player.MusicPlayerController
import com.example.presentation.FullScreenLyricsView
import com.example.presentation.MusicViewModel
import com.example.repository.MusicRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LyricsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var app: Application
    private lateinit var viewModel: MusicViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        val db = MusicDatabase.getDatabase(app)
        val repo = MusicRepository(db.songDao(), db.playlistDao())
        val playerController = MusicPlayerController(app, repo)
        viewModel = MusicViewModel(app, repo, playerController)
    }

    @Test
    fun testFullScreenLyricsView_noLyrics() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            artist = "Artist",
            album = "Album",
            duration = 180000L,
            size = 1000L,
            path = "/dummy/path.mp3",
            lyricsSynced = null,
            lyricsEmbedded = null
        )

        composeTestRule.setContent {
            FullScreenLyricsView(
                viewModel = viewModel,
                song = song,
                isPlaying = false,
                playbackPosition = 0L,
                artBitmap = null,
                artDominantColor = null,
                onPlayPauseClick = {},
                onSeekTo = {},
                onCloseLyrics = {}
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testFullScreenLyricsView_syncedLyrics() {
        val lrc = """
            [00:01.00]First line of lyrics
            <First:1.0:1.5|line:1.5:2.0|of:2.0:2.5|lyrics:2.5:3.0>
            [00:05.00]Second line with words
            <Second:5.0:5.5|line:5.5:6.0|with:6.0:6.5|words:6.5:7.0>
            [00:10.00]Third line plain
        """.trimIndent()

        val song = Song(
            id = 2L,
            title = "Synced Song",
            artist = "Artist",
            album = "Album",
            duration = 180000L,
            size = 1000L,
            path = "/dummy/path2.mp3",
            lyricsSynced = lrc,
            lyricsEmbedded = null
        )

        composeTestRule.setContent {
            FullScreenLyricsView(
                viewModel = viewModel,
                song = song,
                isPlaying = true,
                playbackPosition = 2000L,
                artBitmap = null,
                artDominantColor = null,
                onPlayPauseClick = {},
                onSeekTo = {},
                onCloseLyrics = {}
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testLyricsSettingsScreen() {
        composeTestRule.setContent {
            com.example.presentation.LyricsSettingsScreen(
                viewModel = viewModel,
                onBackClick = {}
            )
        }
        composeTestRule.waitForIdle()
    }
}
