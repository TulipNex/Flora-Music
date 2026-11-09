package com.example.presentation

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.lyrics.LyricLine
import com.example.lyrics.LyricState
import com.example.lyrics.LyricsParser
import com.example.ui.theme.*
import com.example.utils.MusicScanner
import com.example.utils.AudioArtwork
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.composed
import androidx.compose.material3.ripple
import androidx.compose.material3.LocalUseFallbackRippleImplementation

import androidx.compose.foundation.LocalIndication

fun Modifier.bounceClick(onClick: () -> Unit) = bounceClick(showIndication = true, onClick = onClick, onLongClick = null)

fun Modifier.bounceClick(
    showIndication: Boolean,
    onClick: () -> Unit
) = bounceClick(showIndication = showIndication, onClick = onClick, onLongClick = null)

fun Modifier.bounceClick(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?
) = bounceClick(showIndication = true, onClick = onClick, onLongClick = onLongClick)

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bounceClick(
    showIndication: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BounceClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = if (showIndication) LocalIndication.current else null,
            onClick = onClick,
            onLongClick = onLongClick
        )
}

// Screen navigation enum
enum class FloraScreen {
    HOME, SONGS, PLAYLISTS, ALBUMS, ARTISTS, SEARCH, SETTINGS, TAB_SETTINGS, LYRICS_SETTINGS
}

// Sealed hierarchy for type-safe router states
sealed interface RouterState {
    data class PlaylistDetail(val playlist: Playlist) : RouterState
    data class AlbumDetail(val albumName: String) : RouterState
    data class ArtistDetail(val artistName: String) : RouterState
    data class GenreDetail(val genreName: String) : RouterState
    data class MainScreen(val screen: FloraScreen) : RouterState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloraMainScreen(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Request permissions dynamically
    val permissionString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permissionString) == PackageManager.PERMISSION_GRANTED
        )
    }

    var lastToastTime by remember { mutableStateOf(0L) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val storageGranted = results[permissionString] == true
        hasPermission = storageGranted
        if (storageGranted) {
            viewModel.scanLocalMedia(silent = true)
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastToastTime > 5000L) {
                Toast.makeText(context, "Izin penyimpanan ditolak. Flora tidak dapat mencari musik.", Toast.LENGTH_LONG).show()
                lastToastTime = currentTime
            }
        }
    }

    // Auto-scan on permission granted when app first starts
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.scanLocalMedia(silent = true)
        }
    }

    var currentScreen by rememberSaveable { mutableStateOf(FloraScreen.HOME) }
    var activePlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }
    var activeAlbumName by rememberSaveable { mutableStateOf<String?>(null) }
    var activeArtistName by rememberSaveable { mutableStateOf<String?>(null) }
    var activeGenreName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var isFullPlayerVisible by rememberSaveable { mutableStateOf(false) }

    // Hoisted separate LazyListStates for each of the Home screen categories to preserve scroll positions
    val laguScrollState = rememberLazyListState()
    val albumScrollState = rememberLazyListState()
    val artistScrollState = rememberLazyListState()
    val genreScrollState = rememberLazyListState()
    val favoritScrollState = rememberLazyListState()
    val playlistScrollState = rememberLazyListState()

    val homeScrollState = remember(selectedCategory) {
        when (selectedCategory) {
            "Lagu" -> laguScrollState
            "Album" -> albumScrollState
            "Artis" -> artistScrollState
            "Genre" -> genreScrollState
            "Favorit" -> favoritScrollState
            "Playlist" -> playlistScrollState
            else -> laguScrollState
        }
    }

    // Unified system/physical Back button handler to ensure premium navigation flows
    val isDetailOpen = activePlaylistForDetail != null || activeAlbumName != null || activeArtistName != null || activeGenreName != null
    val isSubScreenOpen = currentScreen != FloraScreen.HOME

    BackHandler(enabled = isDetailOpen || isSubScreenOpen || isFullPlayerVisible) {
        when {
            isFullPlayerVisible -> isFullPlayerVisible = false
            activePlaylistForDetail != null -> activePlaylistForDetail = null
            activeAlbumName != null -> activeAlbumName = null
            activeArtistName != null -> activeArtistName = null
            activeGenreName != null -> activeGenreName = null
            currentScreen != FloraScreen.HOME -> currentScreen = FloraScreen.HOME
        }
    }

    // State from ViewModel
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanTotal by viewModel.scanTotal.collectAsState()

    val currentSong by viewModel.playerController.currentSong.collectAsState()
    val isPlaying by viewModel.playerController.isPlaying.collectAsState()

    var showPlaylistManagerDialog by remember { mutableStateOf(false) }
    var songToAddPlaylist by remember { mutableStateOf<Song?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(
                    visible = !isFullPlayerVisible && currentSong != null,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(400)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(400))
                ) {
                    currentSong?.let { song ->
                        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                            MiniPlayerBar(
                                song = song,
                                isPlaying = isPlaying,
                                onPlayPauseClick = { viewModel.playerController.togglePlay() },
                                onNextClick = { viewModel.playerController.next() },
                                onBarClick = { isFullPlayerVisible = true },
                                progressFlow = viewModel.playerController.playbackPosition,
                                durationFlow = viewModel.playerController.duration
                            )
                        }
                    }
                }
            },
        containerColor = DarkBackground
    ) { paddingValues ->
        val scale by animateFloatAsState(
            targetValue = if (isFullPlayerVisible) 0.93f else 1.0f,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "ScreenScale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isFullPlayerVisible) 0.6f else 1.0f,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "ScreenAlpha"
        )
        val translateY by animateFloatAsState(
            targetValue = if (isFullPlayerVisible) (-40f) else 0f,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "ScreenTranslateY"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    this.translationY = translateY
                }
        ) {
            if (!hasPermission) {
                PermissionEmptyState(onRequestPermission = { launcher.launch(permissionsToRequest) })
			} else if (isScanning) {
                ScanningProgressState(progress = scanProgress, total = scanTotal)
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Maintain the Main UI context in composition always so its lists, tabs, 
                    // scroll positions, are fully preserved with zero flicker or recomposition lag.
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            val pushScreens = listOf(FloraScreen.SONGS, FloraScreen.SEARCH, FloraScreen.SETTINGS, FloraScreen.TAB_SETTINGS)
                            val isPushing = targetState in pushScreens && initialState == FloraScreen.HOME
                            val isPopping = initialState in pushScreens && targetState == FloraScreen.HOME

                            val duration = 300
                            val easing = FastOutSlowInEasing

                            if (isPushing) {
                                (slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } +
                                        fadeIn(animationSpec = tween(duration))) togetherWith
                                        (slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it / 3 } +
                                                fadeOut(animationSpec = tween(duration)))
                            } else if (isPopping) {
                                (slideInHorizontally(animationSpec = tween(duration, easing = easing)) { -it / 3 } +
                                        fadeIn(animationSpec = tween(duration))) togetherWith
                                        (slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { it } +
                                                fadeOut(animationSpec = tween(duration)))
                            } else {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(120))
                            }
                        },
                        label = "MainScreenAnimatedContent",
                        modifier = Modifier.fillMaxSize()
                    ) { screen ->
                        when (screen) {
                            FloraScreen.HOME -> HomeScreen(
                                viewModel = viewModel,
                                selectedCategory = selectedCategory,
                                onCategorySelect = { selectedCategory = it },
                                scrollState = homeScrollState,
                                onScreenNavigate = { currentScreen = it },
                                onPlaylistSelect = { activePlaylistForDetail = it },
                                onAlbumSelect = { activeAlbumName = it },
                                onArtistSelect = { activeArtistName = it },
                                onGenreSelect = { activeGenreName = it },
                                onSongClick = { song, list -> viewModel.playSong(song, list) },
                                onAddToPlaylist = { s ->
                                    songToAddPlaylist = s
                                    showPlaylistManagerDialog = true
                                }
                            )
                            FloraScreen.SONGS -> AllSongsScreen(
                                viewModel = viewModel,
                                onSongClick = { song, list -> viewModel.playSong(song, list) },
                                onAddToPlaylist = { s ->
                                    songToAddPlaylist = s
                                    showPlaylistManagerDialog = true
                                },
                                onBackClick = { currentScreen = FloraScreen.HOME }
                            )
                            FloraScreen.PLAYLISTS -> PlaylistsTab(
                                viewModel = viewModel,
                                onPlaylistSelect = { activePlaylistForDetail = it }
                            )
                            FloraScreen.SEARCH -> SearchTabScreen(
                                viewModel = viewModel,
                                onSongClick = { song, list -> viewModel.playSong(song, list) },
                                onAddToPlaylist = { s ->
                                    songToAddPlaylist = s
                                    showPlaylistManagerDialog = true
                                },
                                onBackClick = { currentScreen = FloraScreen.HOME }
                            )
                            FloraScreen.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { currentScreen = FloraScreen.HOME },
                                onNavigate = { currentScreen = it }
                            )
                            FloraScreen.TAB_SETTINGS -> TabSettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { currentScreen = FloraScreen.SETTINGS }
                            )
                            FloraScreen.LYRICS_SETTINGS -> LyricsSettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { currentScreen = FloraScreen.SETTINGS }
                            )
                            else -> Box(modifier = Modifier.fillMaxSize())
                        }
                    }

                    // 2. Overlay detail screens smoothly on top of the persistent main screen
                    val detailState = remember(activePlaylistForDetail, activeAlbumName, activeArtistName, activeGenreName) {
                        when {
                            activePlaylistForDetail != null -> RouterState.PlaylistDetail(activePlaylistForDetail!!)
                            activeAlbumName != null -> RouterState.AlbumDetail(activeAlbumName!!)
                            activeArtistName != null -> RouterState.ArtistDetail(activeArtistName!!)
                            activeGenreName != null -> RouterState.GenreDetail(activeGenreName!!)
                            else -> null
                        }
                    }

                    AnimatedContent(
                        targetState = detailState,
                        transitionSpec = {
                            val duration = 300
                            val easing = FastOutSlowInEasing
                            if (targetState != null) {
                                // Transition detail in: smooth slide-in from right and fade-in
                                (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(duration, easing = easing)) + fadeIn(animationSpec = tween(duration))) togetherWith
                                        fadeOut(animationSpec = tween(duration))
                            } else {
                                // Transition detail out: slide-out to right 
                                fadeIn(animationSpec = tween(duration)) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(duration, easing = easing)) + fadeOut(animationSpec = tween(duration)))
                            }
                        },
                        label = "DetailAnimatedContent",
                        modifier = Modifier.fillMaxSize()
                    ) { state ->
                        if (state != null) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = DarkBackground // seamless match
                            ) {
                                when (state) {
                                    is RouterState.PlaylistDetail -> {
                                        PlaylistDetailScreen(
                                            playlist = state.playlist,
                                            viewModel = viewModel,
                                            onBackClick = { activePlaylistForDetail = null },
                                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                                            onAddToPlaylist = { s ->
                                                songToAddPlaylist = s
                                                showPlaylistManagerDialog = true
                                            }
                                        )
                                    }
                                    is RouterState.AlbumDetail -> {
                                        AlbumSongsDetailScreen(
                                            albumName = state.albumName,
                                            viewModel = viewModel,
                                            onBackClick = { activeAlbumName = null },
                                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                                            onAddToPlaylist = { s ->
                                                songToAddPlaylist = s
                                                showPlaylistManagerDialog = true
                                            }
                                        )
                                    }
                                    is RouterState.ArtistDetail -> {
                                        ArtistSongsDetailScreen(
                                            artistName = state.artistName,
                                            viewModel = viewModel,
                                            onBackClick = { activeArtistName = null },
                                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                                            onAddToPlaylist = { s ->
                                                songToAddPlaylist = s
                                                showPlaylistManagerDialog = true
                                            }
                                        )
                                    }
                                    is RouterState.GenreDetail -> {
                                        GenreSongsDetailScreen(
                                            genreName = state.genreName,
                                            viewModel = viewModel,
                                            onBackClick = { activeGenreName = null },
                                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                                            onAddToPlaylist = { s ->
                                                songToAddPlaylist = s
                                                showPlaylistManagerDialog = true
                                            }
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }

            // Playlist managers selection overlay modal
            if (showPlaylistManagerDialog && songToAddPlaylist != null) {
                AddToPlaylistDialog(
                    song = songToAddPlaylist!!,
                    viewModel = viewModel,
                    onDismiss = {
                        showPlaylistManagerDialog = false
                        songToAddPlaylist = null
                    }
                )
            }
        }
    }
}

    // Full Player slide overlay on top of Scaffold
    AnimatedVisibility(
        visible = isFullPlayerVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(400)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(400))
    ) {
        FullPlayerScreen(
            viewModel = viewModel,
            onDismiss = { isFullPlayerVisible = false },
            onAddToPlaylist = { s ->
                songToAddPlaylist = s
                showPlaylistManagerDialog = true
            }
        )
    }
}

// 1. Permission empty state Composable
@Composable
fun PermissionEmptyState(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AudioFile,
            contentDescription = "Penyimpanan",
            modifier = Modifier.size(110.dp),
            tint = EmeraldGreen.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Flora Music Player",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Untuk mendeteksi, membaca metadata lengkap, dan memutar lagu-lagu offline Anda secara lokal di perangkat ini, Flora memerlukan akses ke media audio perangkat Anda.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(0.8f)
                .testTag("grant_permission_button")
        ) {
            Text("Berikan Izin Akses", fontWeight = FontWeight.Bold, color = SoftWhite)
        }
    }
}

// 2. Scanning Progress Screen
@Composable
fun ScanningProgressState(progress: Int, total: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = EmeraldGreen,
            strokeWidth = 4.dp,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Memindai Penyimpanan Lokal",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Flora sedang membaca file audio dan mengekstrak metadata lirik lrc langsung dari file atau lirik sidecar...",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (total > 0) {
            Text(
                text = "${progress} / ${total} File Terdeteksi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = EmeraldLight
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progress.toFloat() / total.toFloat(),
                color = EmeraldGreen,
                trackColor = DividerSlate,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        } else {
            Text(
                text = "Mendeteksi file...",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

// 3. Mini Player Component
@Composable
fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onBarClick: () -> Unit,
    progressFlow: StateFlow<Long>,
    durationFlow: StateFlow<Long>
) {
    val progress by progressFlow.collectAsState()
    val duration by durationFlow.collectAsState()

    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(song.path, song.title, song.artist, song.customCoverUri, song.dateModified) {
            ImageRequest.Builder(context)
                .data(AudioArtwork(song.path, song.customCoverUri, song.dateModified))
                .size(100, 100)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build()
        }
    )
    val state = painter.state

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF1C221F)) // Distinct dark gray background from DarkBackground (0xFF0A0D0B)
            .bounceClick(onClick = onBarClick)
            .testTag("mini_player_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CardSlate),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "None",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Image(
                    painter = painter,
                    contentDescription = "Artwork",
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (song.artist.isNotBlank() && song.artist != "<unknown>") song.artist else "Artis Tidak Dikenal",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Tiny Linear Progress at bottom
        if (duration > 0) {
            val ratio = progress.toFloat() / duration.toFloat()
            LinearProgressIndicator(
                progress = ratio.coerceIn(0f, 1f),
                color = EmeraldLight,
                trackColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

// 4. HOME TAB
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    scrollState: LazyListState,
    onScreenNavigate: (FloraScreen) -> Unit,
    onPlaylistSelect: (Playlist) -> Unit,
    onAlbumSelect: (String) -> Unit,
    onArtistSelect: (String) -> Unit,
    onGenreSelect: (String) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val favorites by viewModel.favoriteSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val recentlyAdded by viewModel.recentlyAdded.collectAsState()
    val recentlyAddedValue by viewModel.recentlyAddedValue.collectAsState()
    val recentlyAddedUnit by viewModel.recentlyAddedUnit.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val albumCoverPrefs by viewModel.albumCoverPrefs.collectAsState()
    val artistCoverPrefs by viewModel.artistCoverPrefs.collectAsState()

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val columnsCount = if (isLandscape) 6 else 3

    val isGridView by viewModel.isGridView.collectAsState()

    val tabConfigs by viewModel.tabConfigs.collectAsState()
    val customTabIcons by viewModel.customTabIcons.collectAsState()

    val displayMainTabs = remember(tabConfigs) {
        tabConfigs.map { it.name }
    }
    
    // Ensure selectedCategory is valid
    LaunchedEffect(displayMainTabs, selectedCategory) {
        if (selectedCategory !in displayMainTabs && displayMainTabs.isNotEmpty()) {
            onCategorySelect(displayMainTabs.first())
        }
    }

    var songSortOrder by remember { mutableStateOf("Judul") }
    var songSortAscending by remember { mutableStateOf(true) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var selectedSongForDetails by remember { mutableStateOf<Song?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    // Aggregate album and artists dynamically from database
    val uniqueAlbumsWithSong = remember(allSongs) {
        allSongs.groupBy { it.album }.filterKeys { it.isNotBlank() }
    }
    val uniqueArtistsWithSong = remember(allSongs) {
        val map = java.util.TreeMap<String, MutableList<Song>>(String.CASE_INSENSITIVE_ORDER)
        allSongs.forEach { song ->
            val artists = if (song.artist.isBlank() || song.artist == "<unknown>") {
                listOf("Artis Tidak Dikenal")
            } else {
                song.artist.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            artists.forEach { artist ->
                map.getOrPut(artist) { mutableListOf() }.add(song)
            }
        }
        map
    }

    val uniqueGenresWithSong = remember(allSongs) {
        val map = java.util.TreeMap<String, MutableList<Song>>(String.CASE_INSENSITIVE_ORDER)
        allSongs.forEach { song ->
            val genres = if (song.genre.isBlank() || song.genre == "<unknown>" || song.genre.equals("Unknown", ignoreCase = true)) {
                listOf("Genre Tidak Dikenal")
            } else {
                song.genre.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            genres.forEach { genre ->
                map.getOrPut(genre) { mutableListOf() }.add(song)
            }
        }
        map
    }

    val sortedSongs = remember(allSongs, songSortOrder, songSortAscending) {
        val sorted = when (songSortOrder) {
            "Judul" -> allSongs.sortedBy { it.title.lowercase() }
            "Tahun Rilis" -> allSongs.sortedBy { it.year }
            "Durasi" -> allSongs.sortedBy { it.duration }
            "Tanggal Ditambahkan" -> allSongs.sortedBy { it.dateAdded }
            else -> allSongs
        }
        if (songSortAscending) sorted else sorted.reversed()
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Flora",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Button
                    IconButton(
                        onClick = { onScreenNavigate(FloraScreen.SEARCH) },
                        modifier = Modifier
                            .background(CardSlate, CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Pencarian",
                            tint = EmeraldGreen
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = { onScreenNavigate(FloraScreen.SETTINGS) },
                        modifier = Modifier
                            .background(CardSlate, CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = EmeraldGreen
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

        // Row of shortcuts / quick horizontal category navigation bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("homepage_category_bar"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
                items(displayMainTabs) { tab ->
                    val customIconName = customTabIcons[tab]
                    val icon = if (customIconName != null) {
                        when (customIconName) {
                            "api" -> Icons.Default.Whatshot
                            "bintang" -> Icons.Default.Star
                            "bulan" -> Icons.Default.DarkMode
                            "game/console" -> Icons.Default.VideogameAsset
                            "olahraga" -> Icons.Default.FitnessCenter
                            "buku" -> Icons.Default.MenuBook
                            "gitar" -> Icons.Default.MusicNote
                            "headphone" -> Icons.Default.Headphones
                            "kamera" -> Icons.Default.CameraAlt
                            "film" -> Icons.Default.Movie
                            "mobil" -> Icons.Default.DirectionsCar
                            "pesawat" -> Icons.Default.Flight
                            "laptop" -> Icons.Default.Laptop
                            "belanja" -> Icons.Default.ShoppingCart
                            "tas" -> Icons.Default.Work
                            else -> Icons.Default.Folder
                        }
                    } else {
                        when (tab) {
                            "Beranda" -> Icons.Default.Home
                            "Lagu" -> Icons.Default.MusicNote
                            "Album" -> Icons.Default.Album
                            "Artis" -> Icons.Default.Person
                            "Favorit" -> Icons.Default.Favorite
                            "Riwayat" -> Icons.Default.History
                            "Teratas" -> Icons.Default.TrendingUp
                            "Terbaru" -> Icons.Default.FiberNew
                            "Playlist" -> Icons.Default.PlaylistPlay
                            "Genre" -> Icons.Default.Category
                            "Koleksi" -> Icons.Default.LibraryMusic
                            else -> Icons.Default.MusicNote
                        }
                    }
                    CategoryPill(
                        label = tab,
                        icon = icon,
                        isSelected = selectedCategory == tab,
                        onClick = { onCategorySelect(tab) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))

        // Scrolling content
        LazyColumn(
            state = scrollState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // CONDITIONAL RENDERING BASED ON SELECTED CATEGORY
            val currentConfig = tabConfigs.find { it.name == selectedCategory }
            val currentItems = currentConfig?.items ?: emptyList()
            if (currentItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Tab kosong.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            if (currentItems.contains("Lagu")) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sortedSongs.size} Lagu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .background(CardSlate, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Urutkan",
                                tint = EmeraldGreen
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(CardSlate)
                        ) {
                            Text(
                                text = "Berdasarkan",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            val options = listOf("Judul", "Tahun Rilis", "Durasi", "Tanggal Ditambahkan")
                            options.forEach { opt ->
                                val isSelected = songSortOrder == opt
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = opt,
                                            color = if (isSelected) EmeraldGreen else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        songSortOrder = opt
                                        showSortMenu = false
                                    }
                                )
                            }
                            
                            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))
                            
                            Text(
                                text = "Urutan",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            val orderOptions = listOf("Naik", "Turun")
                            orderOptions.forEach { opt ->
                                val isAscending = opt == "Naik"
                                val isSelected = songSortAscending == isAscending
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = opt,
                                            color = if (isSelected) EmeraldGreen else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        songSortAscending = isAscending
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (sortedSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(CardSlate, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Koleksi lagu kosong. Pindai musik lokal Anda.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                if (isGridView) {
                    val chunkedSongs = sortedSongs.chunked(columnsCount)
                    items(chunkedSongs, key = { row -> row.map { it.id }.joinToString("-") }) { rowSongs ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowSongs.forEach { song ->
                                Box(modifier = Modifier.weight(1f)) {
                                    RecentSongCard(
                                        song = song,
                                        onClick = { onSongClick(song, sortedSongs) },
                                        onLongClick = { selectedSongForOptions = song }
                                    )
                                }
                            }
                            repeat(columnsCount - rowSongs.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(sortedSongs, key = { it.id }) { song ->
                        HomeSongListRowItem(
                            song = song,
                            isCurrent = false,
                            onClick = { onSongClick(song, sortedSongs) },
                            onOptionClick = { selectedSongForOptions = song }
                        )
                    }
                }
            }
        }

        if (currentItems.contains("Album")) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Eksplor Album", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${uniqueAlbumsWithSong.size} Album", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (uniqueAlbumsWithSong.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(CardSlate, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Album kosong. Pindai musik lokal Anda.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                if (isGridView) {
                    val chunkedAlbums = uniqueAlbumsWithSong.keys.toList().chunked(columnsCount)
                    items(chunkedAlbums, key = { row -> row.joinToString("-") }) { rowAlbums ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowAlbums.forEach { album ->
                                val albumSongs = uniqueAlbumsWithSong[album] ?: emptyList()
                                Box(modifier = Modifier.weight(1f)) {
                                    AlbumGridCell(
                                        albumName = album,
                                        songs = albumSongs,
                                        preferredCoverPath = albumCoverPrefs[album],
                                        onClick = { onAlbumSelect(album) }
                                    )
                                }
                            }
                            repeat(columnsCount - rowAlbums.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    val listKeys = uniqueAlbumsWithSong.keys.toList()
                    items(listKeys, key = { it }) { album ->
                        val albumSongs = uniqueAlbumsWithSong[album] ?: emptyList()
                        HomeAlbumListRowItem(
                            albumName = album,
                            songs = albumSongs,
                            preferredCoverPath = albumCoverPrefs[album],
                            onClick = { onAlbumSelect(album) }
                        )
                    }
                }
            }
        }

        if (currentItems.contains("Artis")) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Artis", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${uniqueArtistsWithSong.size} Artis", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (uniqueArtistsWithSong.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(CardSlate, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Artis kosong. Pindai musik lokal Anda.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                if (isGridView) {
                    val chunkedArtists = uniqueArtistsWithSong.keys.toList().chunked(columnsCount)
                    items(chunkedArtists, key = { row -> row.joinToString("-") }) { rowArtists ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowArtists.forEach { artist ->
                                val artistSongs = uniqueArtistsWithSong[artist] ?: emptyList()
                                Box(modifier = Modifier.weight(1f)) {
                                    ArtistGridCell(
                                        artistName = artist,
                                        songs = artistSongs,
                                        preferredCoverPath = artistCoverPrefs[artist],
                                        onClick = { onArtistSelect(artist) }
                                    )
                                }
                            }
                            repeat(columnsCount - rowArtists.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    val listKeys = uniqueArtistsWithSong.keys.toList()
                    items(listKeys, key = { it }) { artist ->
                        val artistSongs = uniqueArtistsWithSong[artist] ?: emptyList()
                        HomeArtistListRowItem(
                            artistName = artist,
                            songs = artistSongs,
                            preferredCoverPath = artistCoverPrefs[artist],
                            onClick = { onArtistSelect(artist) }
                        )
                    }
                }
            }
        }

        if (currentItems.contains("Favorit") && currentItems.size == 1) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Lagu Favorit Anda", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${favorites.size} Lagu", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (favorites.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Belum ada lagu favorit.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                if (isGridView) {
                    val chunkedFavs = favorites.chunked(columnsCount)
                    items(chunkedFavs, key = { row -> "fav_main_" + row.joinToString("-") { it.id.toString() } }) { rowFavs ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowFavs.forEach { song -> Box(modifier = Modifier.weight(1f)) { RecentSongCard(song = song, onClick = { onSongClick(song, favorites) }, onLongClick = { selectedSongForOptions = song }) } }
                            repeat(columnsCount - rowFavs.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(favorites, key = { "fav_main_list_" + it.id }) { song ->
                        HomeSongListRowItem(song = song, isCurrent = false, onClick = { onSongClick(song, favorites) }, onOptionClick = { selectedSongForOptions = song })
                    }
                }
            }
        }

        if (currentItems.contains("Riwayat") && currentItems.size == 1) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Baru Saja Diputar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${recentlyPlayed.size} Lagu", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (recentlyPlayed.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat pemutaran.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                if (isGridView) {
                    val chunked = recentlyPlayed.chunked(columnsCount)
                    items(chunked, key = { row -> "hist_main_" + row.joinToString("-") { it.id.toString() } }) { rowSongs ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowSongs.forEach { song -> Box(modifier = Modifier.weight(1f)) { RecentSongCard(song = song, onClick = { onSongClick(song, recentlyPlayed) }, onLongClick = { selectedSongForOptions = song }) } }
                            repeat(columnsCount - rowSongs.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(recentlyPlayed, key = { "hist_main_list_" + it.id }) { song ->
                        HomeSongListRowItem(song = song, isCurrent = false, onClick = { onSongClick(song, recentlyPlayed) }, onOptionClick = { selectedSongForOptions = song })
                    }
                }
            }
        }

        if (currentItems.contains("Teratas") && currentItems.size == 1) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Paling Sering Diputar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${mostPlayed.size} Lagu", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (mostPlayed.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Belum ada lagu yang sering diputar.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                if (isGridView) {
                    val chunked = mostPlayed.chunked(columnsCount)
                    items(chunked, key = { row -> "top_main_" + row.joinToString("-") { it.id.toString() } }) { rowSongs ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowSongs.forEach { song -> Box(modifier = Modifier.weight(1f)) { RecentSongCard(song = song, onClick = { onSongClick(song, mostPlayed) }, onLongClick = { selectedSongForOptions = song }) } }
                            repeat(columnsCount - rowSongs.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(mostPlayed, key = { "top_main_list_" + it.id }) { song ->
                        HomeSongListRowItem(song = song, isCurrent = false, onClick = { onSongClick(song, mostPlayed) }, onOptionClick = { selectedSongForOptions = song })
                    }
                }
            }
        }

        if (currentItems.contains("Terbaru") && currentItems.size == 1) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Lagu Terbaru Anda", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${recentlyAdded.size} Lagu", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (recentlyAdded.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Belum ada lagu baru dalam $recentlyAddedValue $recentlyAddedUnit terakhir.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                if (isGridView) {
                    val chunked = recentlyAdded.chunked(columnsCount)
                    items(chunked, key = { row -> "new_main_" + row.joinToString("-") { it.id.toString() } }) { rowSongs ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowSongs.forEach { song -> Box(modifier = Modifier.weight(1f)) { RecentSongCard(song = song, onClick = { onSongClick(song, recentlyAdded) }, onLongClick = { selectedSongForOptions = song }) } }
                            repeat(columnsCount - rowSongs.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(recentlyAdded, key = { "new_main_list_" + it.id }) { song ->
                        HomeSongListRowItem(song = song, isCurrent = false, onClick = { onSongClick(song, recentlyAdded) }, onOptionClick = { selectedSongForOptions = song })
                    }
                }
            }
        }

        if (currentItems.contains("Playlist") && currentItems.size == 1) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Daftar Playlist", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${allPlaylists.size} Playlist", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)).clickable { showCreatePlaylistDialog = true }, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = "Buat Playlist Baru", tint = EmeraldGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Buat Playlist Baru", color = EmeraldGreen, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (isGridView) {
                val chunkedPlaylists = allPlaylists.chunked(columnsCount)
                items(chunkedPlaylists, key = { row -> "pl_main_" + row.joinToString("-") { it.id.toString() } }) { rowPlaylists ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowPlaylists.forEach { playlist -> Box(modifier = Modifier.weight(1f)) { PlaylistGridCell(playlist = playlist, onClick = { onPlaylistSelect(playlist) }) } }
                        repeat(columnsCount - rowPlaylists.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            } else {
                items(allPlaylists, key = { "pl_main_list_" + it.id }) { plt ->
                    HomePlaylistListRowItem(playlist = plt, onClick = { onPlaylistSelect(plt) })
                }
            }
        }

        if (currentItems.contains("Genre") && currentItems.size == 1) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Genre Musik", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${uniqueGenresWithSong.size} Genre", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (uniqueGenresWithSong.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Genre kosong.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                if (isGridView) {
                    val chunkedGenres = uniqueGenresWithSong.keys.toList().chunked(columnsCount)
                    items(chunkedGenres, key = { row -> "genre_main_" + row.joinToString("-") }) { rowGenres ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowGenres.forEach { genre ->
                                val genreSongs = uniqueGenresWithSong[genre] ?: emptyList()
                                Box(modifier = Modifier.weight(1f)) { GenreGridCell(genreName = genre, songs = genreSongs, onClick = { onGenreSelect(genre) }) }
                            }
                            repeat(columnsCount - rowGenres.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                } else {
                    val listKeys = uniqueGenresWithSong.keys.toList()
                    items(listKeys, key = { "genre_main_list_" + it }) { genre ->
                        val genreSongs = uniqueGenresWithSong[genre] ?: emptyList()
                        HomeGenreListRowItem(genreName = genre, songs = genreSongs, onClick = { onGenreSelect(genre) })
                    }
                }
            }
        }

        if (currentItems.size == 1 && currentItems.first().startsWith("CUSTOM_")) {
            val customName = currentItems.first().removePrefix("CUSTOM_")
            val customIconName = customTabIcons[customName]
            val icon = if (customIconName != null) {
                when (customIconName) {
                    "api" -> Icons.Default.Whatshot
                    "bintang" -> Icons.Default.Star
                    "bulan" -> Icons.Default.DarkMode
                    "game/console" -> Icons.Default.VideogameAsset
                    "olahraga" -> Icons.Default.FitnessCenter
                    "buku" -> Icons.Default.MenuBook
                    "gitar" -> Icons.Default.MusicNote
                    "headphone" -> Icons.Default.Headphones
                    "kamera" -> Icons.Default.CameraAlt
                    "film" -> Icons.Default.Movie
                    "mobil" -> Icons.Default.DirectionsCar
                    "pesawat" -> Icons.Default.Flight
                    "laptop" -> Icons.Default.Laptop
                    "belanja" -> Icons.Default.ShoppingCart
                    "tas" -> Icons.Default.Work
                    else -> Icons.Default.Folder
                }
            } else {
                Icons.Default.Folder
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                    Text(customName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Text("Tab kosong.", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }

        if (currentItems.size > 1) {
            currentItems.filter { it != currentConfig?.name }.forEach { itemKey ->
                when {
                    itemKey == "Favorit" -> {
                        item {
                            SectionHeader("Favorit", onSeeAll = null)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (favorites.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text("Belum ada lagu favorit.", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        } else {
                            if (isGridView) {
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        contentPadding = PaddingValues(bottom = 12.dp)
                                    ) {
                                        items(favorites, key = { "fav_${it.id}" }) { song ->
                                            RecentSongCard(
                                                song = song,
                                                onClick = { onSongClick(song, favorites) },
                                                onLongClick = { selectedSongForOptions = song }
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(favorites, key = { "fav_list_${it.id}" }) { song ->
                                    HomeSongListRowItem(
                                        song = song,
                                        isCurrent = false,
                                        onClick = { onSongClick(song, favorites) },
                                        onOptionClick = { selectedSongForOptions = song }
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    itemKey == "Riwayat" -> {
                        item {
                            SectionHeader("Riwayat", onSeeAll = null)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (recentlyPlayed.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text("Belum ada riwayat putar.", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        } else {
                            if (isGridView) {
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        contentPadding = PaddingValues(bottom = 12.dp)
                                    ) {
                                        items(recentlyPlayed, key = { "hist_${it.id}" }) { song ->
                                            RecentSongCard(
                                                song = song,
                                                onClick = { onSongClick(song, recentlyPlayed) },
                                                onLongClick = { selectedSongForOptions = song }
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(recentlyPlayed, key = { "hist_list_${it.id}" }) { song ->
                                    HomeSongListRowItem(
                                        song = song,
                                        isCurrent = false,
                                        onClick = { onSongClick(song, recentlyPlayed) },
                                        onOptionClick = { selectedSongForOptions = song }
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    itemKey == "Teratas" -> {
                        item {
                            SectionHeader("Teratas", onSeeAll = null)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (mostPlayed.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text("Belum ada lagu yang sering diputar.", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        } else {
                            if (isGridView) {
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        contentPadding = PaddingValues(bottom = 12.dp)
                                    ) {
                                        items(mostPlayed, key = { "top_${it.id}" }) { song ->
                                            RecentSongCard(
                                                song = song,
                                                onClick = { onSongClick(song, mostPlayed) },
                                                onLongClick = { selectedSongForOptions = song }
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(mostPlayed, key = { "top_list_${it.id}" }) { song ->
                                    HomeSongListRowItem(
                                        song = song,
                                        isCurrent = false,
                                        onClick = { onSongClick(song, mostPlayed) },
                                        onOptionClick = { selectedSongForOptions = song }
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    itemKey == "Terbaru" -> {
                        item {
                            SectionHeader("Terbaru", onSeeAll = null)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (recentlyAdded.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text("Belum ada lagu baru dalam $recentlyAddedValue $recentlyAddedUnit terakhir.", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        } else {
                            if (isGridView) {
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        contentPadding = PaddingValues(bottom = 12.dp)
                                    ) {
                                        items(recentlyAdded, key = { "new_${it.id}" }) { song ->
                                            RecentSongCard(
                                                song = song,
                                                onClick = { onSongClick(song, recentlyAdded) },
                                                onLongClick = { selectedSongForOptions = song }
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(recentlyAdded, key = { "new_list_${it.id}" }) { song ->
                                    HomeSongListRowItem(
                                        song = song,
                                        isCurrent = false,
                                        onClick = { onSongClick(song, recentlyAdded) },
                                        onOptionClick = { selectedSongForOptions = song }
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    itemKey == "Playlist" -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Daftar Playlist", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("(${allPlaylists.size})", fontSize = 14.sp, color = TextSecondary)
                                }

                                Button(
                                    onClick = { showCreatePlaylistDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("create_playlist_button_home")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Buat Playlist",
                                        modifier = Modifier.size(16.dp),
                                        tint = SoftWhite
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Buat",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftWhite
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (allPlaylists.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(CardSlate, RoundedCornerShape(12.dp))
                                        .clickable { showCreatePlaylistDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Belum ada playlist manual.",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Tekan tombol + Buat di atas untuk membuat!",
                                            color = EmeraldGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            if (isGridView) {
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        contentPadding = PaddingValues(bottom = 12.dp)
                                    ) {
                                        items(allPlaylists, key = { "pl_${it.id}" }) { playlist ->
                                            PlaylistGridCell(
                                                playlist = playlist,
                                                onClick = { onPlaylistSelect(playlist) }
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(allPlaylists, key = { "pl_list_${it.id}" }) { plt ->
                                    HomePlaylistListRowItem(
                                        playlist = plt,
                                        onClick = { onPlaylistSelect(plt) }
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    itemKey == "Genre" -> {
                        item {
                            SectionHeader("Genre", onSeeAll = null)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        if (uniqueGenresWithSong.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text("Genre kosong. Pindai musik lokal Anda.", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        } else {
                            if (isGridView) {
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        contentPadding = PaddingValues(bottom = 12.dp)
                                    ) {
                                        items(uniqueGenresWithSong.keys.toList(), key = { "genre_$it" }) { genre ->
                                            GenreGridCell(
                                                genreName = genre,
                                                songs = uniqueGenresWithSong[genre] ?: emptyList(),
                                                onClick = { onGenreSelect(genre) }
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(uniqueGenresWithSong.keys.toList(), key = { "genre_list_$it" }) { genre ->
                                    HomeGenreListRowItem(
                                        genreName = genre,
                                        songs = uniqueGenresWithSong[genre] ?: emptyList(),
                                        onClick = { onGenreSelect(genre) }
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    itemKey.startsWith("CUSTOM_") -> {
                        val customName = itemKey.removePrefix("CUSTOM_")
                        val customIconName = customTabIcons[customName]
                        val icon = if (customIconName != null) {
                            when (customIconName) {
                                "api" -> Icons.Default.Whatshot
                                "bintang" -> Icons.Default.Star
                                "bulan" -> Icons.Default.DarkMode
                                "game/console" -> Icons.Default.VideogameAsset
                                "olahraga" -> Icons.Default.FitnessCenter
                                "buku" -> Icons.Default.MenuBook
                                "gitar" -> Icons.Default.MusicNote
                                "headphone" -> Icons.Default.Headphones
                                "kamera" -> Icons.Default.CameraAlt
                                "film" -> Icons.Default.Movie
                                "mobil" -> Icons.Default.DirectionsCar
                                "pesawat" -> Icons.Default.Flight
                                "laptop" -> Icons.Default.Laptop
                                "belanja" -> Icons.Default.ShoppingCart
                                "tas" -> Icons.Default.Work
                                else -> Icons.Default.Folder
                            }
                        } else {
                            Icons.Default.Folder
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                                Text(text = customName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(CardSlate, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Subtab kosong.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Playlist Baru", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nama Playlist", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = DividerSlate,
                        focusedLabelColor = EmeraldGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("playlist_name_field_home")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Buat Playlist", color = SoftWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceSlate
        )
    }

    if (selectedSongForOptions != null) {
        val song = selectedSongForOptions!!
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { selectedSongForOptions = null },
            onOpenArtist = {
                selectedSongForOptions = null
                onArtistSelect(song.artist)
            },
            onOpenAlbum = {
                selectedSongForOptions = null
                onAlbumSelect(song.album)
            },
            onAddToPlaylist = {
                selectedSongForOptions = null
                onAddToPlaylist(song)
            },
            onShowDetail = {
                val s = song
                selectedSongForOptions = null
                selectedSongForDetails = s
            },
            onShare = {
                selectedSongForOptions = null
                try {
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Lagu"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal membagikan lagu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onDelete = {
                val s = song
                selectedSongForOptions = null
                songToDelete = s
            }
        )
    }

    if (selectedSongForDetails != null) {
        SongDetailsDialog(
            song = selectedSongForDetails!!,
            onDismiss = { selectedSongForDetails = null }
        )
    }

    if (songToDelete != null) {
        val song = songToDelete!!
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Hapus Lagu", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus '${song.title}' dari penyimpanan dan pustaka?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSong(song)
                        songToDelete = null
                        Toast.makeText(context, "Lagu '${song.title}' dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceSlate
        )
    }
}

@Composable
fun FolderBlacklistDialog(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val availableFolders by viewModel.allAvailableFolders.collectAsState()
    val blacklistedFolders by viewModel.blacklistedFolders.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Daftar Hitam Folder",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Folder yang aktif di daftar hitam lagunya akan disembunyikan dari koleksi Anda.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (availableFolders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada folder yang dimuat saat ini.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableFolders.toList().sorted()) { folderPath ->
                            val isBlacklisted = folderPath in blacklistedFolders
                            val folderName = try {
                                java.io.File(folderPath).name.ifBlank { "Root" }
                            } catch (e: Exception) {
                                folderPath
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardSlate)
                                    .clickable {
                                        viewModel.setFolderBlacklisted(folderPath, !isBlacklisted)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = folderName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBlacklisted) TextSecondary else Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = folderPath,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Switch(
                                    checked = isBlacklisted,
                                    onCheckedChange = { checked ->
                                        viewModel.setFolderBlacklisted(folderPath, checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmeraldGreen,
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = SurfaceSlate
                                    ),
                                    modifier = Modifier.scale(0.85f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Tutup",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Subcomponents helper for Home screen
@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        if (onSeeAll != null) {
            TextButton(
                onClick = onSeeAll,
                colors = ButtonDefaults.textButtonColors(contentColor = EmeraldLight)
            ) {
                Text("Lihat")
            }
        }
    }
}

@Composable
fun LibraryStatusCard(
    title: String,
    stat: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(72.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = EmeraldGreen, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 11.sp, color = TextSecondary)
                Text(stat, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CategoryPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .height(42.dp)
            .clip(CircleShape)
            .bounceClick(onClick = onClick)
            .testTag("category_pill_$label"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EmeraldGreen else CardSlate
        ),
        shape = CircleShape,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF0A0D0B) else EmeraldGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF0A0D0B) else Color.White
            )
        }
    }
}

@Composable
fun RecentSongCard(
    song: Song,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(song.path, song.title, song.artist, song.customCoverUri, song.dateModified) {
            ImageRequest.Builder(context)
                .data(AudioArtwork(song.path, song.customCoverUri, song.dateModified))
                .size(150, 150)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build()
        }
    )
    val state = painter.state

    Column(
        modifier = Modifier
            .width(114.dp)
            .bounceClick(showIndication = false, onClick = onClick, onLongClick = onLongClick)
            .graphicsLayer(),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(114.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CardSlate),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(36.dp))
            }
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                        }
                    },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = song.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PlaylistGridCell(playlist: Playlist, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(98.dp)
            .bounceClick(showIndication = false, onClick = onClick)
            .graphicsLayer(),
        colors = CardDefaults.cardColors(containerColor = CardSlate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(Icons.Default.QueueMusic, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
            Column {
                Text(
                    playlist.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Playlist Lokal", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun AlbumGridCell(albumName: String, songs: List<Song>, preferredCoverPath: String? = null, onClick: () -> Unit) {
    val firstSong = songs.firstOrNull()
    val coverPath = preferredCoverPath ?: firstSong?.path
    val coverUri = firstSong?.customCoverUri
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(coverPath, firstSong?.dateModified) {
            ImageRequest.Builder(context)
                .data(coverPath?.let { AudioArtwork(it, coverUri, firstSong?.dateModified ?: 0L) })
                .size(150, 150)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build()
        }
    )
    val state = painter.state

    Column(
        modifier = Modifier
            .width(110.dp)
            .bounceClick(showIndication = false, onClick = onClick)
            .graphicsLayer()
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CardSlate),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Album, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(36.dp))
            }
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                        }
                    },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            albumName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text("${songs.size} Lagu", fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
fun ArtistGridCell(artistName: String, songs: List<Song>, preferredCoverPath: String? = null, onClick: () -> Unit) {
    val context = LocalContext.current
    val painter = if (preferredCoverPath != null) {
        rememberAsyncImagePainter(
            model = remember(preferredCoverPath) {
                ImageRequest.Builder(context)
                    .data(java.io.File(preferredCoverPath))
                    .size(150, 150)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build()
            }
        )
    } else null

    Column(
        modifier = Modifier
            .width(96.dp)
            .bounceClick(showIndication = false, onClick = onClick)
            .graphicsLayer(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(CardSlate),
            contentAlignment = Alignment.Center
        ) {
            if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            artistName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text("${songs.size} Lagu", fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun GenreGridCell(genreName: String, songs: List<Song>, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .bounceClick(showIndication = false, onClick = onClick)
            .graphicsLayer(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(CardSlate),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Category, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            genreName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text("${songs.size} Lagu", fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}


// 5. ALL SONGS TAB
@Composable
fun AllSongsScreen(
    viewModel: MusicViewModel,
    onSongClick: (Song, List<Song>) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onBackClick: () -> Unit
) {
    val songs by viewModel.allSongs.collectAsState()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Text(
                text = "Koleksi Lagu",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        if (songs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Penyimpanan audio lokal kosong. Klik hubungkan atau salin lagu ke folder musik.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(songs) { song ->
                    SongListRowItem(
                        song = song,
                        isCurrent = viewModel.playerController.currentSong.value?.id == song.id,
                        onRowClick = { onSongClick(song, songs) },
                        onOptionClick = { onAddToPlaylist(song) },
                        onFavToggle = { viewModel.toggleFavorite(song) }
                    )
                }
            }
        }
    }
}

// 6. PLAYLIST TAB SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsTab(
    viewModel: MusicViewModel,
    onPlaylistSelect: (Playlist) -> Unit
) {
    val playlists by viewModel.allPlaylists.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Playlist Saya",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.testTag("create_playlist_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Buat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (playlists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Anda belum memiliki playlist. Buatlah untuk merapikan lagu favorit Anda.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(playlists) { play ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable { onPlaylistSelect(play) },
                        colors = CardDefaults.cardColors(containerColor = CardSlate)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(play.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            IconButton(onClick = { viewModel.deletePlaylist(play.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Playlist Baru", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nama Playlist") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = DividerSlate,
                        focusedLabelColor = EmeraldGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("playlist_name_field")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Buat Playlist", color = SoftWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = SurfaceSlate
        )
    }
}

// 7. SEARCH TAB
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTabScreen(
    viewModel: MusicViewModel,
    onSongClick: (Song, List<Song>) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onBackClick: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Text(
                text = "Pencarian Realtime",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Cari judul, artis, album, atau genre...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldGreen) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = DividerSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().testTag("music_search_field"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (query.isBlank()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Masukkan kata kunci pencarian di atas untuk mencocokkan lagu Anda secara instan.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else if (searchResults.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Tidak ada trek lagu yang cocok dengan pencarian \"$query\".",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(searchResults) { song ->
                    SongListRowItem(
                        song = song,
                        isCurrent = viewModel.playerController.currentSong.value?.id == song.id,
                        onRowClick = { onSongClick(song, searchResults) },
                        onOptionClick = { onAddToPlaylist(song) },
                        onFavToggle = { viewModel.toggleFavorite(song) }
                    )
                }
            }
        }
    }
}

// 8. Playlist detail screen
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    LaunchedEffect(playlist.id) {
        viewModel.selectPlaylist(playlist.id)
    }

    val songsInPlaylist by viewModel.songsInSelectedPlaylist.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = playlist.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Playlist Lokal • ${songsInPlaylist.size} Lagu", fontSize = 12.sp, color = TextSecondary)
                }
            }

            IconButton(
                onClick = {
                    showDeleteConfirm = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Playlist",
                    tint = Color.Red.copy(alpha = 0.8f)
                )
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Hapus Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Apakah Anda yakin ingin menghapus playlist \"${playlist.name}\"? Tindakan ini tidak dapat dibatalkan.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePlaylist(playlist.id)
                            showDeleteConfirm = false
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                    ) {
                        Text("Hapus", color = SoftWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Batal", color = TextSecondary)
                    }
                },
                containerColor = SurfaceSlate
            )
        }

        if (songsInPlaylist.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Playlist masih kosong. Kembali dan tambahkan lagu favorit Anda!", color = TextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 82.dp)) {
                items(songsInPlaylist) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SongListRowItem(
                            song = song,
                            isCurrent = viewModel.playerController.currentSong.value?.id == song.id,
                            onRowClick = { onSongClick(song, songsInPlaylist) },
                            onOptionClick = { onAddToPlaylist(song) },
                            onFavToggle = { viewModel.toggleFavorite(song) }
                        )

                        IconButton(onClick = { viewModel.removeSongFromPlaylist(playlist.id, song.id) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

// 9. Album detail screen
@Composable
fun AlbumSongsDetailScreen(
    albumName: String,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val songsInAlbum = remember(allSongs, albumName) {
        allSongs.filter { it.album == albumName }
    }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(albumName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Album • ${songsInAlbum.size} Lagu", fontSize = 12.sp, color = TextSecondary)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 82.dp)) {
            items(songsInAlbum) { song ->
                SongListRowItem(
                    song = song,
                    isCurrent = viewModel.playerController.currentSong.value?.id == song.id,
                    onRowClick = { onSongClick(song, songsInAlbum) },
                    onOptionClick = { onAddToPlaylist(song) },
                    onFavToggle = { viewModel.toggleFavorite(song) },
                    onSetAsAlbumCover = {
                        viewModel.setAlbumCoverPath(albumName, song.path)
                        Toast.makeText(context, "Sampul album diubah", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// 10. Artist detail screen
@Composable
fun ArtistSongsDetailScreen(
    artistName: String,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val songsByArtist = remember(allSongs, artistName) {
        allSongs.filter { song ->
            val artists = if (song.artist.isBlank() || song.artist == "<unknown>") {
                listOf("Artis Tidak Dikenal")
            } else {
                song.artist.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            artists.any { it.equals(artistName, ignoreCase = true) }
        }
    }
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setArtistCoverFromUri(artistName, it)
            Toast.makeText(context, "Sampul artis diubah", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artistName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Artis • ${songsByArtist.size} Lagu", fontSize = 12.sp, color = TextSecondary)
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceSlate)
                ) {
                    DropdownMenuItem(
                        text = { Text("Atur Sampul Artis", color = Color.White) },
                        onClick = {
                            showMenu = false
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Sampul Artis", color = Color.White) },
                        onClick = {
                            showMenu = false
                            viewModel.removeArtistCover(artistName)
                            Toast.makeText(context, "Sampul artis dihapus", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 82.dp)) {
            items(songsByArtist) { song ->
                SongListRowItem(
                    song = song,
                    isCurrent = viewModel.playerController.currentSong.value?.id == song.id,
                    onRowClick = { onSongClick(song, songsByArtist) },
                    onOptionClick = { onAddToPlaylist(song) },
                    onFavToggle = { viewModel.toggleFavorite(song) }
                )
            }
        }
    }
}


// Shared Row Item Composable for all song listings
@Composable
fun SongListRowItem(
    song: Song,
    isCurrent: Boolean,
    onRowClick: () -> Unit,
    onOptionClick: () -> Unit,
    onFavToggle: () -> Unit,
    onSetAsAlbumCover: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(song.path, song.title, song.artist, song.customCoverUri, song.dateModified) {
            ImageRequest.Builder(context)
                .data(AudioArtwork(song.path, song.customCoverUri, song.dateModified))
                .size(100, 100)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build()
        }
    )
    val state = painter.state

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .bounceClick(onClick = onRowClick)
            .graphicsLayer(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) SurfaceSlate else Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CardSlate),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isCurrent) EmeraldGreen else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) EmeraldGreen else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Interactive Actions
            IconButton(onClick = onFavToggle) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorit",
                    tint = if (song.isFavorite) EmeraldGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            var expanded by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceSlate)
                ) {
                    DropdownMenuItem(
                        text = { Text("Tambah ke Playlist", color = Color.White) },
                        onClick = {
                            expanded = false
                            onOptionClick()
                        }
                    )
                    if (onSetAsAlbumCover != null) {
                        DropdownMenuItem(
                            text = { Text("Jadikan Sampul Album", color = Color.White) },
                            onClick = {
                                expanded = false
                                onSetAsAlbumCover()
                            }
                        )
                    }
                }
            }
        }
    }
}

// Add to manual Playlist dialog
@Composable
fun AddToPlaylistDialog(
    song: Song,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val playlists by viewModel.allPlaylists.collectAsState()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Tambah ke Playlist",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    song.title,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (playlists.isEmpty()) {
                    Text(
                        "Belum ada playlist manual. Buat playlist baru terlebih dahulu melalu tab Playlist.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(playlists) { plat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addSongToPlaylist(plat.id, song.id)
                                        Toast.makeText(
                                            context,
                                            "\"${song.title}\" berhasil ditambahkan ke Playlist ${plat.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = EmeraldGreen)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(plat.name, color = Color.White, fontSize = 15.sp)
                            }
                            HorizontalDivider(color = DividerSlate, thickness = 1.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextSecondary)
                    }
                }
            }
        }
    }
}

// FORMAT MILLISECONDS AS 00:00 (min:sec)
fun formatDuration(ms: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

@Composable
fun HomeSongListRowItem(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onOptionClick: () -> Unit
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(song.path, song.title, song.artist, song.customCoverUri, song.dateModified) {
            ImageRequest.Builder(context)
                .data(AudioArtwork(song.path, song.customCoverUri, song.dateModified))
                .size(100, 100)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build()
        }
    )
    val state = painter.state

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .graphicsLayer(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) SurfaceSlate else Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CardSlate),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onOptionClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opsi",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: Song,
    onDismiss: () -> Unit,
    onOpenArtist: () -> Unit,
    onOpenAlbum: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShowDetail: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceSlate,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header: song info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${song.artist} • ${song.album}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 8.dp))

            SongOptionItem(
                icon = Icons.Default.Person,
                title = "Buka Artis",
                onClick = onOpenArtist
            )
            SongOptionItem(
                icon = Icons.Default.Album,
                title = "Buka Album",
                onClick = onOpenAlbum
            )
            SongOptionItem(
                icon = Icons.Default.PlaylistAdd,
                title = "Tambahkan Ke Playlist",
                onClick = onAddToPlaylist
            )
            SongOptionItem(
                icon = Icons.Default.Info,
                title = "Detail",
                onClick = onShowDetail
            )
            SongOptionItem(
                icon = Icons.Default.Share,
                title = "Bagikan",
                onClick = onShare
            )
            SongOptionItem(
                icon = Icons.Default.Delete,
                title = "Hapus",
                tint = Color(0xFFFF5252),
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun SongOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(title, color = tint, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SongDetailsDialog(song: Song, onDismiss: () -> Unit) {
    val durationText = formatDuration(song.duration)
    val file = remember(song.path) { java.io.File(song.path) }
    val fileSize = remember(file) {
        if (file.exists()) {
            val bytes = file.length()
            val mb = bytes.toDouble() / (1024 * 1024)
            String.format(java.util.Locale.US, "%.2f MB", mb)
        } else "-"
    }
    val extension = remember(song.path) { file.extension.uppercase().ifBlank { "AUDIO" } }
    val dateAddedFormatted = remember(song.dateAdded) {
        if (song.dateAdded > 0) {
            val millis = if (song.dateAdded > 10_000_000_000L) song.dateAdded else song.dateAdded * 1000
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(millis))
        } else "-"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detail Lagu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SongDetailItem("Judul", song.title)
                SongDetailItem("Artis", song.artist)
                SongDetailItem("Album", song.album)
                SongDetailItem("Durasi", durationText)
                SongDetailItem("Format", extension)
                SongDetailItem("Ukuran File", fileSize)
                SongDetailItem("Ditambahkan", dateAddedFormatted)
                SongDetailItem("Lokasi File", song.path)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = EmeraldGreen)
            }
        },
        containerColor = SurfaceSlate
    )
}

@Composable
private fun SongDetailItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HomePlaylistListRowItem(playlist: Playlist, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .graphicsLayer(),
        colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSlate),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    playlist.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Playlist Lokal", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun HomeAlbumListRowItem(albumName: String, songs: List<Song>, preferredCoverPath: String? = null, onClick: () -> Unit) {
    val firstSong = songs.firstOrNull()
    val coverPath = preferredCoverPath ?: firstSong?.path
    val coverUri = firstSong?.customCoverUri
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = remember(coverPath, firstSong?.dateModified) {
            ImageRequest.Builder(context)
                .data(coverPath?.let { AudioArtwork(it, coverUri, firstSong?.dateModified ?: 0L) })
                .size(100, 100)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build()
        }
    )
    val state = painter.state

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .graphicsLayer(),
        colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CardSlate),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Album, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                }
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    albumName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${songs.size} Lagu", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun HomeArtistListRowItem(artistName: String, songs: List<Song>, preferredCoverPath: String? = null, onClick: () -> Unit) {
    val context = LocalContext.current
    val painter = if (preferredCoverPath != null) {
        rememberAsyncImagePainter(
            model = remember(preferredCoverPath) {
                ImageRequest.Builder(context)
                    .data(java.io.File(preferredCoverPath))
                    .size(100, 100)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build()
            }
        )
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .graphicsLayer(),
        colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardSlate),
                contentAlignment = Alignment.Center
            ) {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    artistName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${songs.size} Lagu", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun HomeGenreListRowItem(genreName: String, songs: List<Song>, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .graphicsLayer(),
        colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardSlate),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Category, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    genreName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("${songs.size} Lagu", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

// 12. Genre detail screen
@Composable
fun GenreSongsDetailScreen(
    genreName: String,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val songsByGenre = remember(allSongs, genreName) {
        allSongs.filter { song ->
            val genres = if (song.genre.isBlank() || song.genre == "<unknown>" || song.genre.equals("Unknown", ignoreCase = true)) {
                listOf("Genre Tidak Dikenal")
            } else {
                song.genre.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            genres.any { it.equals(genreName, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(genreName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Genre • ${songsByGenre.size} Lagu", fontSize = 12.sp, color = TextSecondary)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 82.dp)) {
            items(songsByGenre) { song ->
                SongListRowItem(
                    song = song,
                    isCurrent = viewModel.playerController.currentSong.value?.id == song.id,
                    onRowClick = { onSongClick(song, songsByGenre) },
                    onOptionClick = { onAddToPlaylist(song) },
                    onFavToggle = { viewModel.toggleFavorite(song) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onNavigate: (FloraScreen) -> Unit
) {
    val context = LocalContext.current
    val appVersionDisplay = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val code = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
            "v${pInfo.versionName ?: com.example.BuildConfig.VERSION_NAME} (Build $code)"
        } catch (e: Exception) {
            "v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})"
        }
    }

    val isGridView by viewModel.isGridView.collectAsState()
    val historyLimit by viewModel.historyLimit.collectAsState()
    val mostPlayedLimit by viewModel.mostPlayedLimit.collectAsState()
    val recentlyAddedValue by viewModel.recentlyAddedValue.collectAsState()
    val recentlyAddedUnit by viewModel.recentlyAddedUnit.collectAsState()

    var showFolderBlacklistDialog by remember { mutableStateOf(false) }
    var showHistoryLimitDialog by remember { mutableStateOf(false) }
    var showMostPlayedLimitDialog by remember { mutableStateOf(false) }
    var showRecentlyAddedRangeDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Pengaturan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 82.dp)
        ) {
            item {
                Text(
                    text = "Tampilan",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                
                ListItem(
                    headlineContent = { Text("Tampilan Grid di Home", color = Color.White) },
                    supportingContent = { Text("Gunakan grid alih-alih list untuk lagu dan kategori", color = TextSecondary) },
                    trailingContent = {
                        Switch(
                            checked = isGridView,
                            onCheckedChange = { viewModel.setGridView(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen, checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f))
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                ListItem(
                    modifier = Modifier.clickable { onNavigate(FloraScreen.TAB_SETTINGS) },
                    headlineContent = { Text("Tab Beranda", color = Color.White) },
                    supportingContent = { Text("Atur urutan dan visibilitas tab", color = TextSecondary) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Batas Tampilan Lagu",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                )

                ListItem(
                    modifier = Modifier.clickable { showHistoryLimitDialog = true },
                    headlineContent = { Text("Batas Lagu Riwayat", color = Color.White) },
                    supportingContent = { Text("Jumlah maksimal lagu yang tampil di riwayat", color = TextSecondary) },
                    trailingContent = {
                        Text(
                            text = if (historyLimit <= 0) "Tanpa Batas" else "$historyLimit Lagu",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                ListItem(
                    modifier = Modifier.clickable { showMostPlayedLimitDialog = true },
                    headlineContent = { Text("Batas Lagu Teratas", color = Color.White) },
                    supportingContent = { Text("Jumlah maksimal lagu terpopuler yang tampil", color = TextSecondary) },
                    trailingContent = {
                        Text(
                            text = if (mostPlayedLimit <= 0) "Tanpa Batas" else "$mostPlayedLimit Lagu",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                ListItem(
                    modifier = Modifier.clickable { showRecentlyAddedRangeDialog = true },
                    headlineContent = { Text("Rentang Waktu Lagu Terbaru", color = Color.White) },
                    supportingContent = { Text("Hanya tampilkan lagu dalam rentang waktu ini", color = TextSecondary) },
                    trailingContent = {
                        Text(
                            text = "$recentlyAddedValue $recentlyAddedUnit",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                Text(
                    text = "UI Player",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ListItem(
                    modifier = Modifier.clickable { onNavigate(FloraScreen.LYRICS_SETTINGS) },
                    headlineContent = { Text("Tampilan Lirik", color = Color.White) },
                    supportingContent = { Text("Sesuaikan gaya dan posisi teks lirik", color = TextSecondary) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                Text(
                    text = "Perpustakaan",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ListItem(
                    modifier = Modifier.clickable { viewModel.scanLocalMedia() },
                    headlineContent = { Text("Pindai Ulang Musik", color = Color.White) },
                    supportingContent = { Text("Perbarui daftar lagu dari penyimpanan", color = TextSecondary) },
                    leadingContent = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                ListItem(
                    modifier = Modifier.clickable { showFolderBlacklistDialog = true },
                    headlineContent = { Text("Daftar Hitam Folder", color = Color.White) },
                    supportingContent = { Text("Sembunyikan lagu dari folder tertentu", color = TextSecondary) },
                    leadingContent = { Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                Text(
                    text = "Tentang",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Versi Aplikasi", color = Color.White) },
                    supportingContent = { Text(appVersionDisplay, color = TextSecondary) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }

    if (showFolderBlacklistDialog) {
        FolderBlacklistDialog(
            viewModel = viewModel,
            onDismiss = { showFolderBlacklistDialog = false }
        )
    }

    if (showHistoryLimitDialog) {
        var tempInput by remember { mutableStateOf(if (historyLimit == 0) "0" else historyLimit.toString()) }
        AlertDialog(
            onDismissRequest = { showHistoryLimitDialog = false },
            containerColor = CardSlate,
            title = { Text("Batas Lagu Riwayat", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Tentukan jumlah maksimal lagu yang disimpan dan ditampilkan pada tab Riwayat. Masukkan 0 untuk tanpa batas.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) tempInput = it },
                        label = { Text("Jumlah Lagu (0 = Tanpa Batas)", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val num = tempInput.toIntOrNull() ?: 50
                        viewModel.setHistoryLimit(num)
                        showHistoryLimitDialog = false
                    }
                ) {
                    Text("Simpan", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHistoryLimitDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    if (showMostPlayedLimitDialog) {
        var tempInput by remember { mutableStateOf(if (mostPlayedLimit == 0) "0" else mostPlayedLimit.toString()) }
        AlertDialog(
            onDismissRequest = { showMostPlayedLimitDialog = false },
            containerColor = CardSlate,
            title = { Text("Batas Lagu Teratas", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Tentukan jumlah maksimal lagu terpopuler yang ditampilkan pada tab Teratas. Masukkan 0 untuk tanpa batas.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) tempInput = it },
                        label = { Text("Jumlah Lagu (0 = Tanpa Batas)", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val num = tempInput.toIntOrNull() ?: 50
                        viewModel.setMostPlayedLimit(num)
                        showMostPlayedLimitDialog = false
                    }
                ) {
                    Text("Simpan", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMostPlayedLimitDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    if (showRecentlyAddedRangeDialog) {
        var tempValue by remember { mutableStateOf(recentlyAddedValue.toString()) }
        var tempUnit by remember { mutableStateOf(recentlyAddedUnit) }
        val units = listOf("Hari", "Minggu", "Bulan", "Tahun")

        AlertDialog(
            onDismissRequest = { showRecentlyAddedRangeDialog = false },
            containerColor = CardSlate,
            title = { Text("Rentang Waktu Lagu Terbaru", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Hanya lagu yang ditambahkan dalam rentang waktu ini yang akan masuk ke tab Terbaru.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempValue,
                        onValueChange = { if (it.all { char -> char.isDigit() }) tempValue = it },
                        label = { Text("Angka", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Parameter Waktu:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        units.forEach { unit ->
                            val isSelected = tempUnit == unit
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldGreen else SurfaceSlate)
                                    .clickable { tempUnit = unit }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unit,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val num = tempValue.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        viewModel.setRecentlyAddedRange(num, tempUnit)
                        showRecentlyAddedRangeDialog = false
                    }
                ) {
                    Text("Simpan", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecentlyAddedRangeDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}
