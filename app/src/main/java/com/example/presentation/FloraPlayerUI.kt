package com.example.presentation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Song
import com.example.lyrics.LyricLine
import com.example.lyrics.LyricState
import com.example.lyrics.LyricsParser
import com.example.ui.theme.*
import com.example.utils.MusicScanner
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.geometry.Offset
import kotlin.math.roundToInt

import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.ui.draw.alpha
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import android.net.Uri

import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import android.graphics.BlurMaskFilter
import java.text.BreakIterator
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.isActive
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    val currentSong by viewModel.playerController.currentSong.collectAsState()
    val isPlaying by viewModel.playerController.isPlaying.collectAsState()
    val playbackPosition by viewModel.playerController.playbackPosition.collectAsState()
    val duration by viewModel.playerController.duration.collectAsState()
    
    val shuffleMode by viewModel.playerController.shuffleMode.collectAsState()
    val repeatMode by viewModel.playerController.repeatMode.collectAsState()
    val playbackSpeed by viewModel.playerController.playbackSpeed.collectAsState()
    val sleepTimerVal by viewModel.playerController.sleepTimerSecondsLeft.collectAsState()


    var showSpeedDialog by rememberSaveable { mutableStateOf(false) }
    var showQueueDialog by rememberSaveable { mutableStateOf(false) }
    var showTimerDialog by rememberSaveable { mutableStateOf(false) }
    var showLyricsView by rememberSaveable { mutableStateOf(false) }
    var showMetaDetailsDialog by rememberSaveable { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = showLyricsView) {
        showLyricsView = false
    }

    if (currentSong == null) {
        onDismiss()
        return
    }

    val song = currentSong!!

    val contextLocal = LocalContext.current
    var songArtBytes by remember(song.path, song.customCoverUri, song.dateModified) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.path, song.customCoverUri, song.dateModified) {
        songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (!song.customCoverUri.isNullOrEmpty()) {
                try {
                    contextLocal.contentResolver.openInputStream(Uri.parse(song.customCoverUri))?.use { it.readBytes() }
                } catch(e: Exception) {
                    MusicScanner.getArtworkBytes(song.path)
                }
            } else {
                MusicScanner.getArtworkBytes(song.path)
            }
        }
    }
    var artBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(songArtBytes) {
        if (songArtBytes != null) {
            artBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    BitmapFactory.decodeByteArray(songArtBytes!!, 0, songArtBytes!!.size)
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            artBitmap = null
        }
    }
    
    var artDominantColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
    LaunchedEffect(artBitmap) {
        if (artBitmap != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val palette = androidx.palette.graphics.Palette.from(artBitmap!!).generate()
                    val vibrant = palette.vibrantSwatch
                    val lightVibrant = palette.lightVibrantSwatch
                    val dominant = palette.dominantSwatch
                    
                    val colorInt = lightVibrant?.rgb ?: vibrant?.rgb ?: dominant?.rgb ?: android.graphics.Color.WHITE
                    artDominantColor = androidx.compose.ui.graphics.Color(colorInt)
                } catch (e: Exception) {
                    artDominantColor = androidx.compose.ui.graphics.Color.White
                }
            }
        } else {
            artDominantColor = null
        }
    }

    val scaleState by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ArtworkScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { }
            }
    ) {
        // 1. DYNAMIC BLURRED ARTWORK BACKGROUND (Lapisan paling belakang)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (artBitmap != null) {
                Image(
                    bitmap = artBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                            }
                        }
                        .blur(radius = 40.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SurfaceSlate, DarkBackground)
                            )
                        )
                )
            }
            // Rich dark gradient to override background and keep UI readable of controls
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.8f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
        }

        // 2. CONTROLLER OR STANDALONE FULLSCREEN LYRICS
        val playerUiAlpha by animateFloatAsState(
            targetValue = if (showLyricsView) 0f else 1f, 
            animationSpec = tween(250, easing = LinearEasing),
            label = "playerUiAlpha"
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = playerUiAlpha }) {
                val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT SIDE panel (Header, Artwork cover, Title, and details)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_player")) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "SEDANG DIPUTAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                letterSpacing = 2.sp
                            )
                            IconButton(onClick = { showMetaDetailsDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Details", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Artwork cover
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = scaleState
                                        scaleY = scaleState
                                    }
                                    .clickable { showLyricsView = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (artBitmap != null) {
                                    Image(
                                        bitmap = artBitmap!!.asImageBitmap(),
                                        contentDescription = "Artwork",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .sizeIn(maxHeight = 150.dp, maxWidth = 150.dp)
                                            .aspectRatio(1f)
                                            .shadow(
                                                elevation = 12.dp,
                                                shape = RoundedCornerShape(16.dp),
                                                clip = false,
                                                ambientColor = EmeraldGreen,
                                                spotColor = Color.Black
                                             )
                                            .clip(RoundedCornerShape(16.dp))
                                            .drawWithCache {
                                                onDrawWithContent {
                                                    drawContent()
                                                }
                                            }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .sizeIn(maxHeight = 150.dp, maxWidth = 150.dp)
                                            .aspectRatio(1f)
                                            .shadow(
                                                elevation = 12.dp,
                                                shape = RoundedCornerShape(16.dp),
                                                clip = false,
                                                ambientColor = EmeraldGreen,
                                                spotColor = Color.Black
                                            )
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(CardSlate),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = EmeraldGreen,
                                            modifier = Modifier.fillMaxSize(0.4f)
                                        )
                                    }
                                }
                            }
                        }

                        // Song metadata and favorite buttons
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = song.title,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f).padding(top = 1.dp)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.toggleFavorite(song) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (song.isFavorite) EmeraldGreen else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onAddToPlaylist(song) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddCircleOutline,
                                                contentDescription = "Add Playlist",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = song.artist,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EmeraldGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // RIGHT SIDE panel (Progress bar, sliders, controls, utilities)
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val ratio = if (duration > 0) playbackPosition.toFloat() / duration.toFloat() else 0f
                        Slider(
                            value = ratio.coerceIn(0f, 1f),
                            onValueChange = { targetRatio ->
                                if (duration > 0) {
                                    val targetPos = (targetRatio * duration).toLong()
                                    viewModel.playerController.seekTo(targetPos)
                                }
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldLight,
                                activeTrackColor = EmeraldGreen,
                                inactiveTrackColor = DividerSlate
                            ),
                            track = { sliderState ->
                                val fraction = if (sliderState.valueRange.endInclusive - sliderState.valueRange.start > 0f) {
                                    (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                                } else {
                                    0f
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(DividerSlate, RoundedCornerShape(1.5.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .align(Alignment.CenterStart)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                                .fillMaxHeight()
                                                .background(EmeraldGreen, RoundedCornerShape(1.5.dp))
                                        )
                                    }
                                }
                            },
                            thumb = {
                                Box(
                                    modifier = Modifier.size(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(EmeraldLight, CircleShape)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("playback_slider")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatDuration(playbackPosition), color = TextSecondary, fontSize = 11.sp)
                            Text(formatDuration(duration), color = TextSecondary, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = { viewModel.playerController.setShuffle(!shuffleMode) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Shuffle",
                                        tint = if (shuffleMode) EmeraldGreen else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = { viewModel.playerController.previous() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen)
                                        .clickable { viewModel.playerController.togglePlay() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = SoftWhite,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = { viewModel.playerController.next() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                val repeatIconColor = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) EmeraldGreen else Color.White.copy(alpha = 0.6f)
                                IconButton(
                                    onClick = { viewModel.playerController.toggleRepeatMode() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                        contentDescription = "Repeat",
                                        tint = repeatIconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Secondary Options Footer
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showLyricsView = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(32.dp).testTag("lyrics_toggle")
                            ) {
                                Text("Lirik", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                            }
                            
                            Button(
                                onClick = { showQueueDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Text("Antrian", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                            }

                            Button(
                                onClick = { showTimerDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Text(
                                    text = if (sleepTimerVal != null) if (sleepTimerVal!! >= 3600) String.format("%02d:%02d:%02d", sleepTimerVal!! / 3600, (sleepTimerVal!! % 3600) / 60, sleepTimerVal!! % 60) else String.format("%02d:%02d", sleepTimerVal!! / 60, sleepTimerVal!! % 60) else "Timer",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sleepTimerVal != null) EmeraldGreen else EmeraldLight
                                )
                            }
                        }
                    }
                }
            } else {
                // Portrait Standard Premium Player Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sleek Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_player")) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "SEDANG DIPUTAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            letterSpacing = 2.sp
                        )

                        IconButton(onClick = { showMetaDetailsDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Details", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TRANSFORMATION COVER (album artwork breathing size)
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .graphicsLayer {
                                    scaleX = scaleState
                                    scaleY = scaleState
                                }
                                .clickable { showLyricsView = true },
                            contentAlignment = Alignment.Center
                        ) {
                            val artworkPadding = 10.dp
                            if (artBitmap != null) {
                                Image(
                                    bitmap = artBitmap!!.asImageBitmap(),
                                    contentDescription = "Artwork",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = artworkPadding)
                                        .aspectRatio(1f)
                                        .shadow(
                                            elevation = 16.dp,
                                            shape = RoundedCornerShape(24.dp),
                                            clip = false,
                                            ambientColor = EmeraldGreen,
                                            spotColor = Color.Black
                                        )
                                        .clip(RoundedCornerShape(24.dp))
                                        .drawWithCache {
                                            onDrawWithContent {
                                                drawContent()
                                            }
                                        }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = artworkPadding)
                                        .aspectRatio(1f)
                                        .shadow(
                                            elevation = 16.dp,
                                            shape = RoundedCornerShape(24.dp),
                                            clip = false,
                                            ambientColor = EmeraldGreen,
                                            spotColor = Color.Black
                                        )
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(CardSlate),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.fillMaxSize(0.4f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // COMMAND BLOCK TERPUSAT (metadata directly at bottom of artwork)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .padding(start = 10.dp, end = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                fontSize = 20.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = song.artist,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = EmeraldGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { viewModel.toggleFavorite(song) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (song.isFavorite) EmeraldGreen else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { onAddToPlaylist(song) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "Add Playlist",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // PREMIUM MINIMALIST PROGRESS SLIDER & TIMER
                    val ratio = if (duration > 0) playbackPosition.toFloat() / duration.toFloat() else 0f
                    Slider(
                        value = ratio.coerceIn(0f, 1f),
                        onValueChange = { targetRatio ->
                            if (duration > 0) {
                                val targetPos = (targetRatio * duration).toLong()
                                viewModel.playerController.seekTo(targetPos)
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldLight,
                            activeTrackColor = EmeraldGreen,
                            inactiveTrackColor = DividerSlate
                        ),
                        track = { sliderState ->
                            val fraction = if (sliderState.valueRange.endInclusive - sliderState.valueRange.start > 0f) {
                                (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                            } else {
                                0f
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .background(DividerSlate, RoundedCornerShape(1.5.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .align(Alignment.CenterStart)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                            .fillMaxHeight()
                                            .background(EmeraldGreen, RoundedCornerShape(1.5.dp))
                                    )
                                }
                            }
                        },
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(EmeraldLight, CircleShape)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playback_slider")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatDuration(playbackPosition), color = TextSecondary, fontSize = 12.sp)
                        Text(formatDuration(duration), color = TextSecondary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // PLAYBACK CONTROLS BLOCK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = { viewModel.playerController.setShuffle(!shuffleMode) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (shuffleMode) EmeraldGreen else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = { viewModel.playerController.previous() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Centered circular premium Play/Pause button
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                                    .bounceClick { viewModel.playerController.togglePlay() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = SoftWhite,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = { viewModel.playerController.next() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            val repeatIconColor = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) EmeraldGreen else Color.White.copy(alpha = 0.6f)
                            IconButton(
                                onClick = { viewModel.playerController.toggleRepeatMode() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = "Repeat",
                                    tint = repeatIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(34.dp))

                    // UTILITIES FOOTER BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showLyricsView = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("lyrics_toggle")
                        ) {
                            Text("Lirik", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                        }

                        Button(
                            onClick = { showQueueDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("Antrian", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                        }

                        Button(
                            onClick = { showTimerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text(
                                text = if (sleepTimerVal != null) if (sleepTimerVal!! >= 3600) String.format("%02d:%02d:%02d", sleepTimerVal!! / 3600, (sleepTimerVal!! % 3600) / 60, sleepTimerVal!! % 60) else String.format("%02d:%02d", sleepTimerVal!! / 60, sleepTimerVal!! % 60) else "Timer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sleepTimerVal != null) EmeraldGreen else EmeraldLight
                            )
                        }
                    }
                }
            }
        }
        }
    }

            androidx.compose.animation.AnimatedVisibility(
                visible = showLyricsView,
                enter = androidx.compose.animation.slideInVertically(animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)),
                exit = androidx.compose.animation.slideOutVertically(animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)),
                modifier = androidx.compose.ui.Modifier.fillMaxSize()
            ) {
                FullScreenLyricsView(
                    viewModel = viewModel,
                    song = song,
                    isPlaying = isPlaying,
                    playbackPosition = playbackPosition,
                    artBitmap = artBitmap,
                    artDominantColor = artDominantColor,
                    onPlayPauseClick = { viewModel.playerController.togglePlay() },
                    onSeekTo = { viewModel.playerController.seekTo(it) },
                    onCloseLyrics = { showLyricsView = false }
                )
            }

    // Preset modals
    if (showSpeedDialog) {
        PlaybackSpeedPresetsDialog(
            currentSpeed = playbackSpeed,
            onSpeedSelect = {
                viewModel.playerController.setPlaybackSpeed(it)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false }
        )
    }

    if (showQueueDialog) {
        val queue by viewModel.playerController.queue.collectAsState()
        QueueDialog(
            queue = queue,
            currentSong = song,
            onSongClick = { s -> 
                viewModel.playerController.play(s, queue) 
                showQueueDialog = false
            },
            onDismiss = { showQueueDialog = false }
        )
    }

    if (showTimerDialog) {
        SleepTimerPresetsDialog(
            currentMinutes = sleepTimerVal,
            onTimerSelect = { duration, waitUntilFinished ->
                viewModel.playerController.startSleepTimer(duration, waitUntilFinished)
                showTimerDialog = false
            },
            onCancelTimer = {
                viewModel.playerController.stopSleepTimer()
                showTimerDialog = false
            },
            onDismiss = { showTimerDialog = false }
        )
    }

    if (showMetaDetailsDialog) {
        SongMetadataEditScreen(
            song = song,
            viewModel = viewModel,
            onDismiss = { showMetaDetailsDialog = false }
        )
    }
}

// standalone fullscreen standby lyrics view with interactive click to seek
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenLyricsView(
    viewModel: MusicViewModel,
    song: Song,
    isPlaying: Boolean,
    playbackPosition: Long,
    artBitmap: android.graphics.Bitmap?,
    artDominantColor: androidx.compose.ui.graphics.Color?,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onCloseLyrics: () -> Unit
) {
    val contextLocal = LocalContext.current
    var parsedLyrics by remember(song.id) { mutableStateOf<LyricState>(LyricState.Loading) }
    LaunchedEffect(song.id, song.lyricsSynced, song.lyricsEmbedded) {
        try {
            val database = com.example.database.MusicDatabase.getDatabase(contextLocal)
            val repo = com.example.data.repository.LyricsRepository(contextLocal, database.lyricsDao())
            parsedLyrics = LyricState.Loading
            val data = repo.getLyrics(song)
            if (data != null && data.lyrics.isNotEmpty()) {
                parsedLyrics = if (data.synced) {
                    LyricState.Synced(data.lyrics)
                } else {
                    LyricState.Static(data.lyrics.joinToString("\n") { it.text })
                }
            } else {
                parsedLyrics = LyricState.None
            }
        } catch (e: Exception) {
            android.util.Log.e("FullScreenLyricsView", "Error fetching lyrics", e)
            parsedLyrics = LyricState.None
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val slideOffset = remember { Animatable(0f) }

    fun Modifier.lyricsEdgeDrag(): Modifier = this.pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragStart = {},
            onDragEnd = {
                if (slideOffset.value > 150f) {
                    coroutineScope.launch {
                        slideOffset.animateTo(
                            targetValue = 2000f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        onCloseLyrics()
                    }
                } else {
                    coroutineScope.launch {
                        slideOffset.animateTo(0f, spring())
                    }
                }
            },
            onDragCancel = {
                coroutineScope.launch {
                    slideOffset.animateTo(0f, spring())
                }
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                coroutineScope.launch {
                    slideOffset.snapTo((slideOffset.value + dragAmount).coerceAtLeast(0f))
                }
            }
        )
    }

    val lyricsAlignment by viewModel.lyricsAlignment.collectAsState()
    val lyricsActivePosition by viewModel.lyricsActivePosition.collectAsState()
    val lyricsActiveFontSize by viewModel.lyricsActiveFontSize.collectAsState()
    val lyricsOtherFontSize by viewModel.lyricsOtherFontSize.collectAsState()
    val lyricsSameFontSize by viewModel.lyricsSameFontSize.collectAsState()
    val lyricsUseDominantColor by viewModel.lyricsUseDominantColor.collectAsState()
    val lyricsActiveFontColor by viewModel.lyricsActiveFontColor.collectAsState()
    val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()
    val lyricsContextLines by viewModel.lyricsContextLines.collectAsState()
    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()
    val lyricsMargin by viewModel.lyricsMargin.collectAsState()
    val lyricsItemSpacing by viewModel.lyricsItemSpacing.collectAsState()
    val lyricsLineHeightMultiplier by viewModel.lyricsLineHeightMultiplier.collectAsState()
    val lyricsFontFamily by viewModel.lyricsFontFamily.collectAsState()
    val lyricsCustomFontUri by viewModel.lyricsCustomFontUri.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val customFontFamily = remember(lyricsFontFamily, lyricsCustomFontUri) {
        if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(lyricsCustomFontUri)
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val typeface = android.graphics.Typeface.Builder(pfd.fileDescriptor).build()
                    pfd.close()
                    if (typeface != null) {
                        androidx.compose.ui.text.font.FontFamily(typeface)
                    } else null
                } else null
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            } ?: androidx.compose.ui.text.font.FontFamily.Default
        } else {
            when (lyricsFontFamily) {
                "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                "SANS_SERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                "CURSIVE" -> androidx.compose.ui.text.font.FontFamily.Cursive
                else -> androidx.compose.ui.text.font.FontFamily.Default
            }
        }
    }

    val align = when (lyricsAlignment) {
        "LEFT" -> TextAlign.Left
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Center
    }

    val colAlign = when (lyricsAlignment) {
        "LEFT" -> Alignment.Start
        "RIGHT" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    val activeColor = if (lyricsUseDominantColor) {
        artDominantColor ?: androidx.compose.ui.graphics.Color.White
    } else {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(lyricsActiveFontColor)) } catch(e: Exception) { androidx.compose.ui.graphics.Color.White }
    }
    
    val otherColor = if (lyricsUseDominantColor) {
        (artDominantColor ?: androidx.compose.ui.graphics.Color.White).copy(alpha = 0.5f)
    } else {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(lyricsOtherFontColor)) } catch(e: Exception) { androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .graphicsLayer {
                translationY = slideOffset.value
            }
            .pointerInput(Unit) {
                detectTapGestures { }
            }
    ) {
        // Left Edge Area for dragging to dismiss
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterStart)
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .zIndex(1f) // Ensure it sits on top of the lyrics to intercept drag
                .lyricsEdgeDrag()
        )

        // Right Edge Area for dragging to dismiss
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterEnd)
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .zIndex(1f) // Ensure it sits on top of the lyrics to intercept drag
                .lyricsEdgeDrag()
        )

        // Center Content of the lyrics
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (val state = parsedLyrics) {
                    is LyricState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    is LyricState.None -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Lirik tidak tersedia.",
                                fontSize = 18.sp,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is LyricState.Static -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 24.dp),
                            horizontalAlignment = colAlign,
                            verticalArrangement = Arrangement.Center
                        ) {
                            item {
                                Text(
                                    text = state.text,
                                    fontFamily = customFontFamily,
                                    fontSize = 18.sp,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 32.sp,
                                    textAlign = align,
                                    modifier = Modifier.padding(horizontal = lyricsMargin.dp)
                                )
                            }
                        }
                    }
                    is LyricState.Synced -> {
                        val lines = state.lines
                        val activeIndex = remember(lines, playbackPosition) {
                            LyricsParser.getActiveLineIndex(lines, playbackPosition)
                        }
                        val activeIndexClean = if (activeIndex == -1) 0 else activeIndex
                        val activeLineIndices = remember(activeIndex, lines) {
                            val set = mutableSetOf<Int>()
                            if (activeIndex != -1) {
                                set.add(activeIndex)
                                val activeLine = lines[activeIndex]
                                if (activeLine.isBackground) {
                                    val pairedMain = (activeIndex - 1 downTo 0).firstOrNull { !lines[it].isBackground }
                                    if (pairedMain != null) set.add(pairedMain)
                                }
                            }
                            set
                        }

                        var isAutoScrollLocked by remember { mutableStateOf(true) }
                        var userManualOffset by remember { mutableFloatStateOf(0f) }
                        var flingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
                        val velocityTracker = remember { VelocityTracker() }
                        val decayAnimSpec = remember { exponentialDecay<Float>(frictionMultiplier = 1.8f) }
                        val itemHeights = remember(lines) { mutableStateMapOf<Int, Int>() }
                        var isInitialLayout by remember(lines) { mutableStateOf(true) }
                        val density = LocalDensity.current
                        val scope = rememberCoroutineScope()

                        LaunchedEffect(isAutoScrollLocked, lines) {
                            if (isAutoScrollLocked) {
                                val start = userManualOffset
                                if (kotlin.math.abs(start) < 1f) {
                                    userManualOffset = 0f
                                    return@LaunchedEffect
                                }
                                val anim = Animatable(start)
                                var lastValue = start
                                anim.animateTo(0f, tween((kotlin.math.abs(start) / 4f).toInt().coerceIn(200, 600), easing = FastOutSlowInEasing)) {
                                    userManualOffset += (value - lastValue)
                                    lastValue = value
                                }
                                userManualOffset = 0f
                            }
                        }

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(remember {
                                    object : NestedScrollConnection {
                                        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                                            if (source == NestedScrollSource.UserInput) isAutoScrollLocked = false
                                            return super.onPostScroll(consumed, available, source)
                                        }
                                        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                                            isAutoScrollLocked = false
                                            return super.onPostFling(consumed, available)
                                        }
                                    }
                                })
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            if (isInitialLayout) continue
                                            flingJob?.cancel()
                                            velocityTracker.resetTracking()
                                            isAutoScrollLocked = false
                                            velocityTracker.addPosition(down.uptimeMillis, down.position)
                                            verticalDrag(down.id) { change ->
                                                userManualOffset = (userManualOffset + change.positionChange().y)
                                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                                change.consume()
                                            }
                                            val velocity = velocityTracker.calculateVelocity().y
                                            flingJob = scope.launch {
                                                AnimationState(initialValue = userManualOffset, initialVelocity = velocity).animateDecay(decayAnimSpec) {
                                                    userManualOffset = value
                                                }
                                            }
                                        }
                                    }
                                }
                        ) {
                            val maxHeightPx = constraints.maxHeight.toFloat()
                            val anchorY = when (lyricsActivePosition) {
                                "TOP" -> with(density) { 32.dp.toPx() }
                                "BOTTOM" -> maxHeightPx - with(density) { 100.dp.toPx() }
                                else -> maxHeightPx / 2f - with(density) { 30.dp.toPx() }
                            }
                            val fallbackLineHeight = with(density) { 68.dp.toPx() }

                            val positions = remember(itemHeights.toMap(), activeIndexClean, lines) {
                                val map = mutableMapOf<Int, Float>()
                                if (lines.isEmpty()) return@remember map

                                map[activeIndexClean] = 0f
                                var currentY = 0f
                                for (i in activeIndexClean - 1 downTo 0) {
                                    val height = itemHeights[i]?.toFloat() ?: fallbackLineHeight
                                    val lineBelow = lines.getOrNull(i + 1)
                                    val noGap = lineBelow?.isBackground == true
                                    currentY -= (height + if (noGap) 0f else with(density) { lyricsItemSpacing.dp.toPx() })
                                    map[i] = currentY
                                }
                                currentY = 0f
                                for (i in activeIndexClean until lines.size - 1) {
                                    val height = itemHeights[i]?.toFloat() ?: fallbackLineHeight
                                    val nextLine = lines.getOrNull(i + 1)
                                    val nextNoGap = nextLine?.isBackground == true
                                    currentY += (height + if (nextNoGap) 0f else with(density) { lyricsItemSpacing.dp.toPx() })
                                    map[i + 1] = currentY
                                }
                                map
                            }

                            LaunchedEffect(lines.size) {
                                if (lines.isNotEmpty()) {
                                    isInitialLayout = true
                                    kotlinx.coroutines.withTimeoutOrNull(600) {
                                        snapshotFlow { 
                                            val h = itemHeights.toMap()
                                            val windowStart = (activeIndexClean - 4).coerceAtLeast(0)
                                            val windowEnd = (activeIndexClean + 6).coerceAtMost(lines.size - 1)
                                            (windowStart..windowEnd).all { h.containsKey(it) } 
                                        }.first { it }
                                    }
                                    isInitialLayout = false
                                }
                            }

                            lines.forEachIndexed { listIndex, line ->
                                key(listIndex) {
                                    val distance = kotlin.math.abs(listIndex - activeIndexClean)
                                    val targetOffset = anchorY + (positions[listIndex] ?: ((listIndex - activeIndexClean).toFloat() * fallbackLineHeight))
                                    
                                    // CULL OUT-OF-BOUNDS ITEMS to prevent massive frame drops
                                    val currentEstimatedY = targetOffset + userManualOffset
                                    val isVisible = currentEstimatedY > -1000f && currentEstimatedY < maxHeightPx + 1000f
                                    
                                    if (isVisible) {
                                        val frozenOffset = remember { mutableFloatStateOf(targetOffset) }
                                        LaunchedEffect(isAutoScrollLocked, targetOffset, isInitialLayout) {
                                            if (isAutoScrollLocked || isInitialLayout) frozenOffset.floatValue = targetOffset
                                        }
                                        val animatedOffset by animateFloatAsState(
                                            targetValue = if (isAutoScrollLocked) targetOffset else frozenOffset.floatValue,
                                            animationSpec = if (isInitialLayout || !isAutoScrollLocked) snap<Float>() 
                                                            else {
                                                                tween<Float>(750, (distance * 20).coerceAtMost(200), FastOutSlowInEasing)
                                                            },
                                            label = "lyricStaggeredOffset_$listIndex"
                                        )

                                        val isAudioActive = activeLineIndices.contains(listIndex)

                                        val baseFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize.toFloat()
                                        val colorTarget = if (isAudioActive) activeColor else otherColor
                                        val colorState by animateColorAsState(
                                            targetValue = colorTarget,
                                            animationSpec = tween(400, easing = LinearOutSlowInEasing),
                                            label = "LyrColor_$listIndex"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .layout { m, c -> 
                                                    val p = m.measure(c.copy(maxHeight = Constraints.Infinity))
                                                    if (p.height > 0) {
                                                        itemHeights[listIndex] = p.height
                                                    }
                                                    layout(p.width, 0) { p.place(0, 0) }
                                                }
                                                .offset { IntOffset(0, (animatedOffset + userManualOffset).roundToInt()) }
                                                .padding(horizontal = lyricsMargin.dp)
                                        ) {
                                            
                                            Box(modifier = Modifier.fillMaxWidth().clickable {
                                                isAutoScrollLocked = true
                                                onSeekTo(line.timestampMs)
                                            }) {
                                                val displayText = if (line.isBackground) line.text.replace(Regex("^\\s*\\(|\\)\\s*$"), "").trim() else line.text
                                                if (line.words.isNotEmpty()) {
                                                    FloraWordLevelLyrics(
                                                        mainText = displayText,
                                                        words = line.words,
                                                        isActiveLine = isAudioActive,
                                                        playbackPosition = playbackPosition,
                                                        isPlaying = isPlaying,
                                                        lyricStyle = TextStyle(
                                                            fontFamily = customFontFamily,
                                                            fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = align,
                                                            lineHeight = if (line.isBackground) (baseFontSize * 0.75f * lyricsLineHeightMultiplier).sp else (baseFontSize * lyricsLineHeightMultiplier).sp
                                                        ),
                                                        lineColor = otherColor,
                                                        expressiveAccent = activeColor,
                                                        isBackground = line.isBackground,
                                                        focusedAlpha = 0.5f,
                                                        alignment = align
                                                    )
                                                } else {
                                                    Text(
                                                        text = displayText,
                                                        fontFamily = customFontFamily,
                                                        fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colorState,
                                                        textAlign = align,
                                                        lineHeight = if (line.isBackground) (baseFontSize * 0.75f * lyricsLineHeightMultiplier).sp else (baseFontSize * lyricsLineHeightMultiplier).sp,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    } // end isVisible
                                } // end key
                            }
                        }
                    }

                }
            }
        }
    }
}

}
// 11. SYNCHRONIZED LYRICS VIEWER COMPOSABLE
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SynchronizedLyricsLayout(
    viewModel: MusicViewModel,
    song: Song,
    currentPositionMs: Long,
    isPlaying: Boolean,
    onSeekTo: (Long) -> Unit
) {
    val contextLocal = LocalContext.current
    var parsedLyrics by remember(song.id) { mutableStateOf<LyricState>(LyricState.Loading) }
    LaunchedEffect(song.id, song.lyricsSynced, song.lyricsEmbedded) {
        val database = com.example.database.MusicDatabase.getDatabase(contextLocal)
        val repo = com.example.data.repository.LyricsRepository(contextLocal, database.lyricsDao())
        parsedLyrics = LyricState.Loading
        val data = repo.getLyrics(song)
        if (data != null) {
            parsedLyrics = if (data.synced) {
                LyricState.Synced(data.lyrics)
            } else {
                LyricState.Static(data.lyrics.firstOrNull()?.text ?: "")
            }
        } else {
            parsedLyrics = LyricState.None
        }
    }
    
    var songArtBytes2 by remember(song.path, song.customCoverUri, song.dateModified) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.path, song.dateModified) {
        songArtBytes2 = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.utils.MusicScanner.getArtworkBytes(song.path)
        }
    }
    var artBitmap2 by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(songArtBytes2) {
        if (songArtBytes2 != null) {
            artBitmap2 = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try { android.graphics.BitmapFactory.decodeByteArray(songArtBytes2!!, 0, songArtBytes2!!.size) } catch (e: Exception) { null }
            }
        } else {
            artBitmap2 = null
        }
    }
    var artDominantColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
    LaunchedEffect(artBitmap2) {
        if (artBitmap2 != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val palette = androidx.palette.graphics.Palette.from(artBitmap2!!).generate()
                    val vibrant = palette.vibrantSwatch
                    val lightVibrant = palette.lightVibrantSwatch
                    val dominant = palette.dominantSwatch
                    val colorInt = lightVibrant?.rgb ?: vibrant?.rgb ?: dominant?.rgb ?: android.graphics.Color.WHITE
                    artDominantColor = androidx.compose.ui.graphics.Color(colorInt)
                } catch (e: Exception) {
                    artDominantColor = androidx.compose.ui.graphics.Color.White
                }
            }
        } else {
            artDominantColor = null
        }
    }

    
    val lyricsAlignment by viewModel.lyricsAlignment.collectAsState()
    val lyricsActivePosition by viewModel.lyricsActivePosition.collectAsState()
    val lyricsActiveFontSize by viewModel.lyricsActiveFontSize.collectAsState()
    val lyricsOtherFontSize by viewModel.lyricsOtherFontSize.collectAsState()
    val lyricsSameFontSize by viewModel.lyricsSameFontSize.collectAsState()
    val lyricsUseDominantColor by viewModel.lyricsUseDominantColor.collectAsState()
    val lyricsActiveFontColor by viewModel.lyricsActiveFontColor.collectAsState()
    val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()
    val lyricsContextLines by viewModel.lyricsContextLines.collectAsState()
    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()
    val lyricsMargin by viewModel.lyricsMargin.collectAsState()
    val lyricsItemSpacing by viewModel.lyricsItemSpacing.collectAsState()
    val lyricsLineHeightMultiplier by viewModel.lyricsLineHeightMultiplier.collectAsState()
    val lyricsFontFamily by viewModel.lyricsFontFamily.collectAsState()
    val lyricsCustomFontUri by viewModel.lyricsCustomFontUri.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val customFontFamily = remember(lyricsFontFamily, lyricsCustomFontUri) {
        if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(lyricsCustomFontUri)
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val typeface = android.graphics.Typeface.Builder(pfd.fileDescriptor).build()
                    pfd.close()
                    androidx.compose.ui.text.font.FontFamily(typeface)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } ?: androidx.compose.ui.text.font.FontFamily.Default
        } else {
            when (lyricsFontFamily) {
                "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                "SANS_SERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                "CURSIVE" -> androidx.compose.ui.text.font.FontFamily.Cursive
                else -> androidx.compose.ui.text.font.FontFamily.Default
            }
        }
    }

    val align = when (lyricsAlignment) {
        "LEFT" -> TextAlign.Left
        "RIGHT" -> TextAlign.Right
        else -> TextAlign.Center
    }

    val colAlign = when (lyricsAlignment) {
        "LEFT" -> Alignment.Start
        "RIGHT" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    val activeColor = if (lyricsUseDominantColor) {
        artDominantColor ?: androidx.compose.ui.graphics.Color.White
    } else {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(lyricsActiveFontColor)) } catch(e: Exception) { androidx.compose.ui.graphics.Color.White }
    }
    
    val otherColor = if (lyricsUseDominantColor) {
        (artDominantColor ?: androidx.compose.ui.graphics.Color.White).copy(alpha = 0.5f)
    } else {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(lyricsOtherFontColor)) } catch(e: Exception) { androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f) }
    }


    when (val state = parsedLyrics) {
        is LyricState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        is LyricState.None -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Lirik tidak tersedia.",
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        is LyricState.Static -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalAlignment = colAlign,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Text(
                        text = state.text,
                        fontFamily = customFontFamily,
                        fontSize = 16.sp,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp,
                        textAlign = align,
                        modifier = Modifier.padding(horizontal = lyricsMargin.dp)
                    )
                }
            }
        }
        is LyricState.Synced -> {
            val lines = state.lines
            val activeIndex = remember(lines, currentPositionMs) {
                LyricsParser.getActiveLineIndex(lines, currentPositionMs)
            }
            val activeIndexClean = if (activeIndex == -1) 0 else activeIndex
            val activeLineIndices = remember(activeIndex, lines) {
                val set = mutableSetOf<Int>()
                if (activeIndex != -1) {
                    set.add(activeIndex)
                    val activeLine = lines[activeIndex]
                    if (activeLine.isBackground) {
                        val pairedMain = (activeIndex - 1 downTo 0).firstOrNull { !lines[it].isBackground }
                        if (pairedMain != null) set.add(pairedMain)
                    }
                }
                set
            }

            var isAutoScrollLocked by remember { mutableStateOf(true) }
            var userManualOffset by remember { mutableFloatStateOf(0f) }
            var flingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
            val velocityTracker = remember { VelocityTracker() }
            val decayAnimSpec = remember { exponentialDecay<Float>(frictionMultiplier = 1.8f) }
            val itemHeights = remember(lines) { mutableStateMapOf<Int, Int>() }
            var isInitialLayout by remember(lines) { mutableStateOf(true) }
            val density = LocalDensity.current
            val scope = rememberCoroutineScope()

            LaunchedEffect(isAutoScrollLocked, lines) {
                if (isAutoScrollLocked) {
                    val start = userManualOffset
                    if (kotlin.math.abs(start) < 1f) {
                        userManualOffset = 0f
                        return@LaunchedEffect
                    }
                    val anim = Animatable(start)
                    var lastValue = start
                    anim.animateTo(0f, tween((kotlin.math.abs(start) / 4f).toInt().coerceIn(200, 600), easing = FastOutSlowInEasing)) {
                        userManualOffset += (value - lastValue)
                        lastValue = value
                    }
                    userManualOffset = 0f
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(remember {
                        object : NestedScrollConnection {
                            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                                if (source == NestedScrollSource.UserInput) isAutoScrollLocked = false
                                return super.onPostScroll(consumed, available, source)
                            }
                            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                                isAutoScrollLocked = false
                                return super.onPostFling(consumed, available)
                            }
                        }
                    })
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (isInitialLayout) continue
                                flingJob?.cancel()
                                velocityTracker.resetTracking()
                                isAutoScrollLocked = false
                                velocityTracker.addPosition(down.uptimeMillis, down.position)
                                verticalDrag(down.id) { change ->
                                    userManualOffset = (userManualOffset + change.positionChange().y)
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                    change.consume()
                                }
                                val velocity = velocityTracker.calculateVelocity().y
                                flingJob = scope.launch {
                                    AnimationState(initialValue = userManualOffset, initialVelocity = velocity).animateDecay(decayAnimSpec) {
                                        userManualOffset = value
                                    }
                                }
                            }
                        }
                    }
            ) {
                val maxHeightPx = constraints.maxHeight.toFloat()
                val anchorY = when (lyricsActivePosition) {
                    "TOP" -> with(density) { 32.dp.toPx() }
                    "BOTTOM" -> maxHeightPx - with(density) { 100.dp.toPx() }
                    else -> maxHeightPx / 2f - with(density) { 30.dp.toPx() }
                }
                val fallbackLineHeight = with(density) { 68.dp.toPx() }

                val positions = remember(itemHeights.toMap(), activeIndexClean, lines) {
                    val map = mutableMapOf<Int, Float>()
                    if (lines.isEmpty()) return@remember map

                    map[activeIndexClean] = 0f
                    var currentY = 0f
                    for (i in activeIndexClean - 1 downTo 0) {
                        val height = itemHeights[i]?.toFloat() ?: fallbackLineHeight
                        val lineBelow = lines.getOrNull(i + 1)
                        val noGap = lineBelow?.isBackground == true
                        currentY -= (height + if (noGap) 0f else with(density) { lyricsItemSpacing.dp.toPx() })
                        map[i] = currentY
                    }
                    currentY = 0f
                    for (i in activeIndexClean until lines.size - 1) {
                        val height = itemHeights[i]?.toFloat() ?: fallbackLineHeight
                        val nextLine = lines.getOrNull(i + 1)
                        val nextNoGap = nextLine?.isBackground == true
                        currentY += (height + if (nextNoGap) 0f else with(density) { lyricsItemSpacing.dp.toPx() })
                        map[i + 1] = currentY
                    }
                    map
                }

                LaunchedEffect(lines.size) {
                    if (lines.isNotEmpty()) {
                        isInitialLayout = true
                        snapshotFlow { 
                            val h = itemHeights.toMap()
                            val windowStart = (activeIndexClean - 8).coerceAtLeast(0)
                            val windowEnd = (activeIndexClean + 12).coerceAtMost(lines.size - 1)
                            (windowStart..windowEnd).all { h.containsKey(it) } 
                        }.first { it }
                        isInitialLayout = false
                    }
                }

                lines.forEachIndexed { listIndex, line ->
                    val distance = kotlin.math.abs(listIndex - activeIndexClean)
                    val targetOffset = anchorY + positions.getOrDefault(listIndex, (listIndex - activeIndexClean) * fallbackLineHeight)
                    val frozenOffset = remember { mutableFloatStateOf(targetOffset) }
                    LaunchedEffect(isAutoScrollLocked, targetOffset, isInitialLayout) {
                        if (isAutoScrollLocked || isInitialLayout) frozenOffset.floatValue = targetOffset
                    }
                    val animatedOffset by animateFloatAsState(
                        targetValue = if (isAutoScrollLocked) targetOffset else frozenOffset.floatValue,
                        animationSpec = if (isInitialLayout || !isAutoScrollLocked) snap<Float>() 
                                        else {
                                            tween<Float>(750, (distance * 20).coerceAtMost(200), FastOutSlowInEasing)
                                        },
                        label = "lyricStaggeredOffset_$listIndex"
                    )

                    val isAudioActive = activeLineIndices.contains(listIndex)

                    val baseFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize.toFloat()
                    val minScale = if (lyricsSameFontSize) (lyricsActiveFontSize / baseFontSize) else (lyricsOtherFontSize / baseFontSize)
                    val colorTarget = if (isAudioActive) activeColor else otherColor
                    val colorState by animateColorAsState(
                        targetValue = colorTarget,
                        animationSpec = tween(400, easing = LinearOutSlowInEasing),
                        label = "LyrColor_$listIndex"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .layout { m, c -> 
                                val p = m.measure(c.copy(maxHeight = Constraints.Infinity))
                                layout(p.width, 0) { p.place(0, 0) }
                            }
                            .offset { IntOffset(0, (animatedOffset + userManualOffset).roundToInt()) }
                            .padding(horizontal = lyricsMargin.dp)
                            .onSizeChanged { itemHeights[listIndex] = it.height }
                    ) {
                        
                                    Box(modifier = Modifier.fillMaxWidth().clickable {
                                        isAutoScrollLocked = true
                                        onSeekTo(line.timestampMs)
                                    }) {
                                        val displayText = if (line.isBackground) line.text.replace(Regex("^\\s*\\(|\\)\\s*$"), "").trim() else line.text
                                        if (line.words.isNotEmpty()) {
                                            FloraWordLevelLyrics(
                                                mainText = displayText,
                                                words = line.words,
                                                isActiveLine = isAudioActive,
                                                playbackPosition = currentPositionMs,
                                                isPlaying = isPlaying,
                                                lyricStyle = TextStyle(
                                                    fontFamily = customFontFamily,
                                                    fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = align,
                                                    lineHeight = if (line.isBackground) (baseFontSize * 0.75f * lyricsLineHeightMultiplier).sp else (baseFontSize * lyricsLineHeightMultiplier).sp
                                                ),
                                                lineColor = otherColor,
                                                expressiveAccent = activeColor,
                                                isBackground = line.isBackground,
                                                focusedAlpha = 0.5f,
                                                alignment = align
                                            )
                                        } else {
                                            Text(
                                                text = displayText,
                                                fontFamily = customFontFamily,
                                                fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorState,
                                                textAlign = align,
                                                lineHeight = if (line.isBackground) (baseFontSize * 0.75f * lyricsLineHeightMultiplier).sp else (baseFontSize * lyricsLineHeightMultiplier).sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                    }
                }
            }
        }
    }
}

}
// Dialog definitions
@Composable
fun PlaybackSpeedPresetsDialog(
    currentSpeed: Float,
    onSpeedSelect: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Kecepatan Putar Lagu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                presets.forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSpeedSelect(speed) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${speed}x untuk musik", color = Color.White, fontSize = 14.sp)
                        if (currentSpeed == speed) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen)
                        }
                    }
                    HorizontalDivider(color = DividerSlate, thickness = 1.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerPresetsDialog(
    currentMinutes: Int?,
    onTimerSelect: (Int, Boolean) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    var hours by remember { mutableStateOf(currentMinutes?.let { it / 3600 } ?: 0) }
    var minutes by remember { mutableStateOf(currentMinutes?.let { (it % 3600) / 60 } ?: 0) }
    var seconds by remember { mutableStateOf(currentMinutes?.let { it % 60 } ?: 0) }
    var waitUntilFinished by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Timer", 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Medium, 
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = (-12).dp)
                        .clickable { waitUntilFinished = !waitUntilFinished }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = waitUntilFinished,
                        onCheckedChange = { waitUntilFinished = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldGreen,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Tunggu sampai lagu selesai", color = Color.White, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(
                        range = 0..23,
                        initialValue = hours,
                        modifier = Modifier.weight(1f),
                        onValueChange = { hours = it }
                    )
                    
                    Text(":", fontSize = 36.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                    
                    WheelPicker(
                        range = 0..59,
                        initialValue = minutes,
                        modifier = Modifier.weight(1f),
                        onValueChange = { minutes = it }
                    )
                    
                    Text(":", fontSize = 36.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                    
                    WheelPicker(
                        range = 0..59,
                        initialValue = seconds,
                        modifier = Modifier.weight(1f),
                        onValueChange = { seconds = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentMinutes != null) {
                        TextButton(onClick = {
                            onCancelTimer()
                        }) {
                            Text("Hapus", color = Color.Red, fontSize = 16.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = {
                        onDismiss()
                    }) {
                        Text("Batal", color = Color.White, fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val totalSeconds = hours * 3600 + minutes * 60 + seconds
                            if (totalSeconds > 0) {
                                onTimerSelect(totalSeconds, waitUntilFinished)
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("Set", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongMetadataEditScreen(
    song: Song,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var lyrics by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(song.lyricsEmbedded ?: "")) }
    val playbackPosition by viewModel.playerController.playbackPosition.collectAsState()
    var composer by remember { mutableStateOf(song.composer) }
    var genre by remember { mutableStateOf(song.genre) }
    var writer by remember { mutableStateOf(song.writer) }
    var trackNumber by remember { mutableStateOf(if (song.trackNumber > 0) song.trackNumber.toString() else "") }
    var discNumber by remember { mutableStateOf(song.discNumber) }
    var year by remember { mutableStateOf(if (song.year > 0) song.year.toString() else "") }
    var comment by remember { mutableStateOf(song.comment) }
    var customCoverUri by remember { mutableStateOf<Uri?>(song.customCoverUri?.let { Uri.parse(it) }) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { e.printStackTrace() }
            customCoverUri = it
        }
    }

    val contextLocal = LocalContext.current
    var songArtBytes by remember(song.path, customCoverUri, song.dateModified) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.path, customCoverUri, song.dateModified) {
        songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (customCoverUri != null) {
                try {
                    contextLocal.contentResolver.openInputStream(customCoverUri!!)?.use { it.readBytes() }
                } catch(e: Exception) {
                    com.example.utils.MusicScanner.getArtworkBytes(song.path)
                }
            } else {
                com.example.utils.MusicScanner.getArtworkBytes(song.path)
            }
        }
    }
    var artBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(songArtBytes) {
        if (songArtBytes != null) {
            artBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    android.graphics.BitmapFactory.decodeByteArray(songArtBytes!!, 0, songArtBytes!!.size)
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            artBitmap = null
        }
    }

    val scrollState = rememberScrollState()

    androidx.activity.compose.BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .zIndex(100f)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Row (TopAppBar style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceSlate)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Text(
                    text = "Edit Metadata",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                val manageStorageLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        if (android.os.Environment.isExternalStorageManager()) {
                            viewModel.updateSongMetadata(
                                songId = song.id,
                                newTitle = title,
                                newArtist = artist,
                                newAlbum = album,
                                newLyrics = lyrics.text,
                                newComposer = composer,
                                newGenre = genre,
                                newWriter = writer,
                                newTrackNumber = trackNumber.toIntOrNull() ?: 0,
                                newDiscNumber = discNumber,
                                newYear = year.toIntOrNull() ?: 0,
                                newComment = comment,
                                newCustomCoverUri = customCoverUri?.toString()
                            )
                            onDismiss()
                        } else {
                            android.widget.Toast.makeText(contextLocal, "Izin diperlukan untuk mengedit file fisik.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                TextButton(
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && !android.os.Environment.isExternalStorageManager()) {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                intent.data = android.net.Uri.parse("package:${contextLocal.packageName}")
                                manageStorageLauncher.launch(intent)
                            } catch (e: Exception) {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                manageStorageLauncher.launch(intent)
                            }
                        } else {
                            viewModel.updateSongMetadata(
                                songId = song.id,
                                newTitle = title,
                                newArtist = artist,
                                newAlbum = album,
                                newLyrics = lyrics.text,
                                newComposer = composer,
                                newGenre = genre,
                                newWriter = writer,
                                newTrackNumber = trackNumber.toIntOrNull() ?: 0,
                                newDiscNumber = discNumber,
                                newYear = year.toIntOrNull() ?: 0,
                                newComment = comment,
                                newCustomCoverUri = customCoverUri?.toString()
                            )
                            onDismiss()
                        }
                    }
                ) {
                    Text("SIMPAN", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            }

            // Scrollable fields form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                    // Cover Art Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkBackground)
                        ) {
                            if (artBitmap != null) {
                                Image(
                                    bitmap = artBitmap!!.asImageBitmap(),
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Pilih Gambar", tint = Color.White, modifier = Modifier.size(64.dp))
                                }
                            }
                            
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (customCoverUri != null) {
                                    IconButton(
                                        onClick = { customCoverUri = null },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { launcher.launch("image/*") },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Ubah", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        label = { Text("Artis") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = { Text("Album") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = composer,
                        onValueChange = { composer = it },
                        label = { Text("Komposer") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = writer,
                        onValueChange = { writer = it },
                        label = { Text("Penulis") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = trackNumber,
                            onValueChange = { trackNumber = it },
                            label = { Text("Nomor Trek") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                                focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                            ),
                            singleLine = true, modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discNumber,
                            onValueChange = { discNumber = it },
                            label = { Text("Nomor Kaset") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                                focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                            ),
                            singleLine = true, modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Tahun Rilis") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Komentar") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    
                    OutlinedTextField(
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        label = { Text("Lirik Tertanam") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        minLines = 4, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .focusRequester(focusRequester)
                            .testTag("lyrics-editor-container")
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { focusRequester.requestFocus() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = { 
                            lyrics = lyrics.copy(selection = androidx.compose.ui.text.TextRange(0, lyrics.text.length))
                        }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Pilih Semua", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val clipText = clipboardManager.getText()?.text ?: ""
                            val text = lyrics.text
                            val selection = lyrics.selection
                            val newText = text.substring(0, selection.start) + clipText + text.substring(selection.end)
                            val newCursorIndex = selection.start + clipText.length
                            lyrics = lyrics.copy(
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(newCursorIndex)
                            )
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Tempel", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Metadata Sistem (Readonly)", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                    MetaRow("Lokasi File", song.path)
                    MetaRow("Jumlah Diputar", "${song.playCount} kali")
                }
        }
    }
}

@Composable
fun MetaRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = DividerSlate, thickness = 1.dp)
    }
}

fun sizeDetail(dp: Int) = Modifier.size(dp.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueDialog(
    queue: List<Song>,
    currentSong: Song?,
    onSongClick: (Song) -> Unit,
    onDismiss: () -> Unit
) {
    val currentIdx = queue.indexOfFirst { it.id == currentSong?.id }
    
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (currentIdx >= 0) currentIdx else 0
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(4.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daftar Putar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${queue.size} lagu",
                            fontSize = 13.sp,
                            color = EmeraldLight
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(queue) { index, song ->
                        val isPlaying = index == currentIdx
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSongClick(song) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Playing",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPlaying) EmeraldGreen else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    fontSize = 13.sp,
                                    color = if (isPlaying) EmeraldLight else Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class HyphenGroupWord(
    val pos: Int,
    val size: Int,
    val isLast: Boolean,
    val groupStartMs: Long,
    val groupEndMs: Long
)

private fun String.containsRtl(): Boolean {
    for (c in this) {
        val directionality = Character.getDirectionality(c).toInt()
        if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT.toInt() ||
            directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC.toInt()
        ) {
            return true
        }
    }
    return false
}

private fun String.toGraphemeClusters(): List<String> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<String>()
    val it = java.text.BreakIterator.getCharacterInstance()
    it.setText(this)
    var start = it.first()
    var end = it.next()
    while (end != java.text.BreakIterator.DONE) {
        result.add(substring(start, end))
        start = end
        end = it.next()
    }
    return result
}

@Composable
fun FloraWordLevelLyrics(
    mainText: String,
    words: List<com.example.lyrics.LyricWord>,
    isActiveLine: Boolean,
    playbackPosition: Long,
    isPlaying: Boolean,
    
    
    lyricStyle: TextStyle,
    lineColor: Color,
    expressiveAccent: Color,
    isBackground: Boolean,
    focusedAlpha: Float,
    alignment: TextAlign
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val glowPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
        }
    }
    
    
    var smoothPosition by remember { mutableLongStateOf(playbackPosition) }
    
    LaunchedEffect(isActiveLine, isPlaying) {
        if (isActiveLine && isPlaying) {
            var lastUpdateTime = System.currentTimeMillis()
            var lastPos = playbackPosition
            while (isActive) {
                withFrameMillis {
                    val now = System.currentTimeMillis()
                    if (playbackPosition != lastPos) {
                        lastPos = playbackPosition
                        lastUpdateTime = now
                    }
                    val elapsed = now - lastUpdateTime
                    smoothPosition = lastPos + elapsed
                }
            }
        }
    }
    
    LaunchedEffect(isActiveLine, playbackPosition, isPlaying) {
        if (!isActiveLine || !isPlaying) {
            smoothPosition = playbackPosition
        }
    }

    val (effectiveWords, effectiveToOriginalIdx) = remember(words, isBackground) {
        words.flatMapIndexed { originalIdx, word ->
            val shouldSplit = word.text.contains('-') && word.text.length > 1 &&
                (!true || words.size == 1)
            if (shouldSplit) {
                val segments = mutableListOf<String>()
                var start = 0
                for (i in 0 until word.text.length) {
                    if (word.text[i] == '-') {
                        segments.add(word.text.substring(start, i + 1))
                        start = i + 1
                    }
                }
                if (start < word.text.length) {
                    segments.add(word.text.substring(start))
                }

                if (segments.size > 1) {
                    val totalDuration = word.endTimeMs - word.startTimeMs
                    val segmentDuration = totalDuration / segments.size
                    segments.mapIndexed { index, segmentText ->
                        com.example.lyrics.LyricWord(
                            text = segmentText,
                            startTimeMs = word.startTimeMs + index * segmentDuration,
                            endTimeMs = word.startTimeMs + (index + 1) * segmentDuration,
                            
                        ) to originalIdx
                    }
                } else listOf(word to originalIdx)
            } else listOf(word to originalIdx)
        }.let { data -> data.map { it.first } to data.map { it.second } }
    }

    // Break mainText into grapheme clusters so that multi-codepoint glyphs
    // (Devanagari/Bengali matras, Arabic ligatures, emoji, etc.) are treated
    // as single units throughout the animation pipeline.
    val graphemeClusters = remember(mainText) { mainText.toGraphemeClusters() }
    val clusterCount = graphemeClusters.size
    // For each cluster index, the String offset (Char index) of its first character in mainText.
    // Required because TextLayoutResult.getBoundingBox/getLineForOffset take
    // String offsets (UTF-16/Char indices), not cluster indices.
    val clusterCharOffsets = remember(mainText) {
        IntArray(clusterCount).also { offsets ->
            var charOffset = 0
            graphemeClusters.forEachIndexed { i, cluster ->
                offsets[i] = charOffset
                charOffset += cluster.length
            }
        }
    }

    // wordIdxMap / charInWordMap / wordLenMap are now sized and indexed by
    // CLUSTER INDEX (not codepoint index) so that each visual glyph unit is
    // mapped to exactly one word slot.
    val charToWordData = remember(mainText, effectiveWords, isBackground, graphemeClusters, clusterCharOffsets) {
        val wordIdxMap = IntArray(clusterCount) { -1 }
        val charInWordMap = IntArray(clusterCount)
        val wordLenMap = IntArray(clusterCount) { 1 }
        var currentPos = 0
        var clCursor = 0
        effectiveWords.forEachIndexed { wordIdx, word ->
            val rawWordText = word.text.let {
                if (isBackground) {
                    var t = it
                    if (wordIdx == 0) t = t.replace(Regex("^\\s*\\("), "")
                    if (wordIdx == effectiveWords.size - 1) t = t.replace(Regex("\\)\\s*$"), "")
                    t
                } else it
            }
            val indexInMain = mainText.indexOf(rawWordText, currentPos)
            if (indexInMain != -1) {
                val wordEndInMain = indexInMain + rawWordText.length
                // Advance clCursor to the first cluster at or after indexInMain
                while (clCursor < clusterCount && clusterCharOffsets[clCursor] < indexInMain) {
                    clCursor++
                }
                val firstClIdx = clCursor
                // Collect all clusters in the word range [indexInMain, wordEndInMain)
                val wordClusterIndices = mutableListOf<Int>()
                while (clCursor < clusterCount && clusterCharOffsets[clCursor] < wordEndInMain) {
                    wordClusterIndices.add(clCursor)
                    clCursor++
                }
                val wordClusterLen = wordClusterIndices.size
                wordClusterIndices.forEachIndexed { posInWord, clIdx ->
                    wordIdxMap[clIdx] = wordIdx
                    charInWordMap[clIdx] = posInWord
                    wordLenMap[clIdx] = wordClusterLen
                }
                // Check the cluster at clCursor for a trailing space
                if (clCursor < clusterCount && clusterCharOffsets[clCursor] == wordEndInMain && 
                    wordEndInMain < mainText.length && mainText[wordEndInMain] == ' ') {
                    val spaceClIdx = clCursor
                    wordIdxMap[spaceClIdx] = wordIdx
                    charInWordMap[spaceClIdx] = wordClusterLen
                    wordLenMap[spaceClIdx] = wordClusterLen + 1
                    clCursor++
                }
                currentPos = wordEndInMain
            }
        }
        Triple(wordIdxMap, charInWordMap, wordLenMap)
    }

    val hyphenGroupData = remember(effectiveWords) {
        val map = mutableMapOf<Int, HyphenGroupWord>()
        var currentGroup = mutableListOf<Int>()
        effectiveWords.forEachIndexed { wordIdx, word ->
            currentGroup.add(wordIdx)
            if (!word.text.endsWith("-")) {
                if (currentGroup.size > 1) {
                    val groupSize = currentGroup.size
                    val groupStartMs = (effectiveWords[currentGroup.first()].startTimeMs).toLong()
                    val groupEndMs = (word.endTimeMs).toLong()
                    currentGroup.forEachIndexed { pos, idx ->
                        map[idx] = HyphenGroupWord(pos, groupSize, pos == groupSize - 1, groupStartMs, groupEndMs)
                    }
                }
                currentGroup = mutableListOf()
            }
        }
        map
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxWidthPx = constraints.maxWidth
        val layoutResult = remember(mainText, maxWidthPx, lyricStyle) {
            val safeWidth = if (maxWidthPx == Constraints.Infinity || maxWidthPx <= 0) 10000 else maxWidthPx
            textMeasurer.measure(
                text = mainText,
                style = lyricStyle,
                constraints = Constraints(maxWidth = safeWidth),
                softWrap = true
            )
        }
        
        // Each layout corresponds to one grapheme cluster (the visual unit),
        // not one codepoint. Fixes Devanagari/Bengali matra fragmentation.
        val letterLayouts = remember(mainText, lyricStyle) {
            graphemeClusters.map { cluster -> 
                try {
                    textMeasurer.measure(cluster, lyricStyle)
                } catch (e: Throwable) {
                    textMeasurer.measure(" ", lyricStyle)
                }
            }
        }
        
        val isRtlText = remember(mainText) { mainText.containsRtl() }
        
        androidx.compose.foundation.Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(with(density) { layoutResult.size.height.toDp() })
            .graphicsLayer(
                clip = false,
                compositingStrategy = CompositingStrategy.Offscreen,
            )
        ) {
            if (mainText.isEmpty()) return@Canvas
            if (!isActiveLine) {
                drawText(textLayoutResult = layoutResult, color = lineColor)
            } else {
                if (isRtlText) {
                    val (wordIdxMap, _, _) = charToWordData
                    val wordFactors = effectiveWords.map { word ->
                        val wStartMs = (word.startTimeMs).toLong()
                        val wEndMs = (word.endTimeMs).toLong()
                        val isWordSung = smoothPosition > wEndMs
                        val isWordActive = smoothPosition in wStartMs..wEndMs
                        val sungFactor = if (isWordSung) 1f 
                                        else if (isWordActive) ((smoothPosition - wStartMs).toFloat() / (wEndMs - wStartMs).coerceAtLeast(1)).coerceIn(0f, 1f)
                                        else 0f
                        Triple(sungFactor, isWordSung, isWordActive)
                    }

                    drawText(textLayoutResult = layoutResult, color = lineColor.copy(alpha = focusedAlpha))

                    effectiveWords.indices.forEach { wIdx ->
                        val (sungFactor, isWordSung, isWordActive) = wordFactors[wIdx]
                        
                        var left = Float.MAX_VALUE
                        var right = Float.MIN_VALUE
                        var top = Float.MAX_VALUE
                        var bottom = Float.MIN_VALUE
                        var found = false

                        for (i in 0 until clusterCount) {
                            if (wordIdxMap[i] == wIdx) {
                                val charOffset = clusterCharOffsets[i]
                                if (charOffset in 0 until layoutResult.layoutInput.text.length) {
                                    val bounds = layoutResult.getBoundingBox(charOffset)
                                    left = minOf(left, bounds.left)
                                    right = maxOf(right, bounds.right)
                                    top = minOf(top, bounds.top)
                                    bottom = maxOf(bottom, bounds.bottom)
                                    found = true
                                }
                            }
                        }

                        if (found) {
                            if (isWordSung) {
                                clipRect(left = left, top = top, right = right, bottom = bottom) {
                                    drawText(textLayoutResult = layoutResult, color = expressiveAccent)
                                }
                            } else if (isWordActive && sungFactor > 0f) {
                                clipRect(left = left, top = top, right = right, bottom = bottom) {
                                    drawText(textLayoutResult = layoutResult, color = expressiveAccent.copy(alpha = focusedAlpha + (1f - focusedAlpha) * sungFactor))
                                }
                            }
                        }
                    }
                    return@Canvas
                }

                val (wordIdxMap, charInWordMap, wordLenMap) = charToWordData
                val wordFactors = effectiveWords.map { word ->
                    val wStartMs = (word.startTimeMs).toLong()
                    val wEndMs = (word.endTimeMs).toLong()
                    val isWordSung = smoothPosition > wEndMs
                    val isWordActive = smoothPosition in wStartMs..wEndMs
                    val sungFactor = if (isWordSung) 1f 
                                    else if (isWordActive) ((smoothPosition - wStartMs).toFloat() / (wEndMs - wStartMs).coerceAtLeast(1)).coerceIn(0f, 1f)
                                    else 0f
                    Triple(sungFactor, word, isWordSung)
                }

                val wordWobbles = FloatArray(words.size)
                words.forEachIndexed { wordIdx, word ->
                    val startMs = (word.startTimeMs).toLong()
                    val timeSinceStart = (smoothPosition - startMs).toFloat()
                    val wobble = if (timeSinceStart in 0f..750f) {
                        if (timeSinceStart < 125f) timeSinceStart / 125f
                        else (1f - (timeSinceStart - 125f) / 625f).coerceAtLeast(0f)
                    } else 0f
                    wordWobbles[wordIdx] = wobble
                }

                val lineCurrentPushes = FloatArray(layoutResult.lineCount)
                val lineTotalPushes = FloatArray(layoutResult.lineCount)
                
                // Pre-calculate total pushes per line to handle alignment correctly.
                // Iterate over cluster indices so each visual glyph unit is one slot.
                for (i in 0 until clusterCount) {
                    val charOffset = clusterCharOffsets[i]
                    if (charOffset !in 0 until layoutResult.layoutInput.text.length) continue
                    val lineIdx = layoutResult.getLineForOffset(charOffset).coerceIn(0, (layoutResult.lineCount - 1).coerceAtLeast(0))
                    val wordIdx = wordIdxMap[i]
                    val originalWordIdx = if (wordIdx != -1) effectiveToOriginalIdx[wordIdx] else -1
                    
                    val (sungFactor, wordItem, isWordSung) = if (wordIdx != -1) wordFactors[wordIdx] else Triple(0f, null, false)
                    val wobble = if (originalWordIdx != -1) wordWobbles[originalWordIdx] else 0f
                    
                    var crescendoDeltaX = 0f
                    val groupWord = if (wordIdx != -1) hyphenGroupData[wordIdx] else null
                    if (groupWord != null) {
                        val p = sungFactor
                        val timeSinceEnd = (smoothPosition - groupWord.groupEndMs).toFloat()
                        val exitDuration = 600f
                        val pOut = (timeSinceEnd / exitDuration).coerceIn(0f, 1f)
                        val peakScale = 0.06f
                        val decay = 2.5f
                        val freq = 10.0f
                        val baseScalePerSegment = 0.012f
                        if (pOut > 0f) {
                            val baseAtEnd = groupWord.pos * baseScalePerSegment
                            val totalAtEnd = baseAtEnd + peakScale
                            crescendoDeltaX = totalAtEnd * kotlin.math.exp(-decay * pOut) * kotlin.math.cos(freq * pOut * kotlin.math.PI.toFloat()) * (1f - pOut)
                        } else if (groupWord.isLast) {
                            val base = groupWord.pos * baseScalePerSegment
                            val springPart = peakScale * (1f - kotlin.math.exp(-decay * p) * kotlin.math.cos(freq * p * kotlin.math.PI.toFloat()) * (1f - p))
                            crescendoDeltaX = base + springPart
                        } else {
                            val boost = if (p > 0f) 0.02f * (1f - p) else 0f
                            crescendoDeltaX = (groupWord.pos * baseScalePerSegment) + boost
                        }
                    }

                    val charLp = if (wordItem != null) {
                        val sMs = wordItem.startTimeMs
                        val dur = (wordItem.endTimeMs - wordItem.startTimeMs).toDouble().coerceAtLeast(100.0)
                        val wProg = (smoothPosition.toDouble() - sMs) / dur
                        val cInW = charInWordMap[i].toDouble()
                        val wLen = wordLenMap[i].toDouble()
                        ((wProg - cInW / wLen) * wLen).coerceIn(0.0, 1.0).toFloat()
                    } else 0f

                    val nudgeScale = if (wordItem != null && !isWordSung && sungFactor > 0f) {
                        0.038f * kotlin.math.sin(charLp * kotlin.math.PI.toFloat()) * kotlin.math.exp(-3f * charLp)
                    } else 0f

                    val charScaleX = 1f + (wobble * 0.025f) + crescendoDeltaX + (nudgeScale * 0.3f)
                    val charBounds = layoutResult.getBoundingBox(charOffset)
                    lineTotalPushes[lineIdx] += charBounds.width * (charScaleX - 1f)
                }

                // Main drawing loop: iterate over cluster indices so each visual
                // glyph (including multi-codepoint Devanagari clusters) is one unit.
                for (i in 0 until clusterCount) {
                    val charOffset = clusterCharOffsets[i]
                    if (charOffset !in 0 until layoutResult.layoutInput.text.length) continue
                    val lineIdx = layoutResult.getLineForOffset(charOffset).coerceIn(0, (layoutResult.lineCount - 1).coerceAtLeast(0))
                    val charBounds = layoutResult.getBoundingBox(charOffset)
                    val wordIdx = wordIdxMap[i]
                    val originalWordIdx = if (wordIdx != -1) effectiveToOriginalIdx[wordIdx] else -1
                    
                    val alignShift = when(alignment) {
                        TextAlign.Center -> -lineTotalPushes[lineIdx] / 2f
                        TextAlign.Right -> -lineTotalPushes[lineIdx]
                        else -> 0f
                    }
                    
                    val (sungFactor, wordItem, isWordSung) = if (wordIdx != -1) wordFactors[wordIdx] else Triple(0f, null, false)
                    val wobble = if (originalWordIdx != -1) wordWobbles[originalWordIdx] else 0f
                    val wobbleX = wobble * 0.025f
                    val wobbleY = wobble * 0.015f
                    
                    val charLp = if (wordItem != null) {
                        val sMs = wordItem.startTimeMs
                        val dur = (wordItem.endTimeMs - wordItem.startTimeMs).toDouble().coerceAtLeast(100.0)
                        val wProg = (smoothPosition.toDouble() - sMs) / dur
                        val cInW = charInWordMap[i].toDouble()
                        val wLen = wordLenMap[i].toDouble()
                        ((wProg - cInW / wLen) * wLen).coerceIn(0.0, 1.0).toFloat()
                    } else 0f

                    val shouldGlow = wordItem != null && !isWordSung && sungFactor > 0.001f

                    var crescendoDeltaX = 0f
                    var crescendoDeltaY = 0f
                    val groupWord = if (wordIdx != -1) hyphenGroupData[wordIdx] else null
                    if (groupWord != null) {
                        val p = sungFactor
                        val timeSinceEnd = (smoothPosition - groupWord.groupEndMs).toFloat()
                        val exitDuration = 600f
                        val pOut = (timeSinceEnd / exitDuration).coerceIn(0f, 1f)
                        val peakScale = 0.06f
                        val decay = 3.5f
                        val freq = 5.0f
                        val baseScalePerSegment = 0.012f
                        if (pOut > 0f) {
                            val baseAtEnd = groupWord.pos * baseScalePerSegment
                            val totalAtEnd = baseAtEnd + peakScale
                            val springOut = totalAtEnd * kotlin.math.exp(-decay * pOut) * kotlin.math.cos(freq * pOut * kotlin.math.PI.toFloat()) * (1f - pOut)
                            crescendoDeltaX = springOut
                            crescendoDeltaY = springOut
                        } else if (groupWord.isLast) {
                            val base = groupWord.pos * baseScalePerSegment
                            val springPart = peakScale * (1f - kotlin.math.exp(-decay * p) * kotlin.math.cos(freq * p * kotlin.math.PI.toFloat()) * (1f - p))
                            crescendoDeltaX = base + springPart
                            crescendoDeltaY = base + springPart
                        } else {
                            val boost = if (p > 0f) 0.02f * (1f - p) else 0f
                            val base = (groupWord.pos * baseScalePerSegment) + boost
                            crescendoDeltaX = base
                            crescendoDeltaY = base
                        }
                    }

                    val nudgeStrength = 0.038f
                    val nudgeScale = if (wordItem != null && !isWordSung && sungFactor > 0f) {
                        nudgeStrength * kotlin.math.sin(charLp * kotlin.math.PI.toFloat()) * kotlin.math.exp(-3f * charLp)
                    } else 0f
                    
                    val charScaleX = 1f + wobbleX + crescendoDeltaX + nudgeScale * 0.3f
                    val charScaleY = 1f + wobbleY + crescendoDeltaY + nudgeScale

                    withTransform({
                        var waveOffset = 0f
                        if (groupWord != null) {
                            val wallTime = System.currentTimeMillis()
                            val adjSmoothPos = smoothPosition
                            val timeInGroup = (adjSmoothPos - groupWord.groupStartMs).toFloat()
                            val timeToGroupEnd = (groupWord.groupEndMs - adjSmoothPos).toFloat()
                            val waveFade = (timeInGroup / 200f).coerceIn(0f, 1f) * (timeToGroupEnd / 200f).coerceIn(0f, 1f)
                            if (waveFade > 0.01f) {
                                val waveSpeed = 0.006f
                                val waveHeight = 3.24f
                                val phaseOffset = i * 0.4f
                                waveOffset = kotlin.math.sin(wallTime * waveSpeed + phaseOffset) * waveHeight * waveFade
                            }
                        }

                        translate(left = alignShift + lineCurrentPushes[lineIdx] + charBounds.left, top = charBounds.top + waveOffset)
                        if (wordIdx != -1) {
                            scale(
                                charScaleX,
                                charScaleY,
                                pivot = Offset(charBounds.width / 2f, charBounds.height)
                            )
                        }
                    }) {
                        if (shouldGlow) {
                            val sMs = wordItem.startTimeMs
                            val eMs = wordItem.endTimeMs
                            val dur = eMs - sMs
                            val wordLenText = wordItem.text.length.coerceAtLeast(1)
                            val impactRatio = dur.toFloat() / wordLenText
                            val fadeFactor = (sungFactor * 5f).coerceIn(0f, 1f) * ((1f - sungFactor) * 8f).coerceIn(0f, 1f)
                            val impactFactor = (((impactRatio - 100f) / 250f).coerceIn(0f, 1f) * 0.6f + ((dur.toFloat() - 300f) / 1500f).coerceIn(0f, 1f) * 0.4f).coerceIn(0f, 1f) * fadeFactor
                            if (impactFactor > 0.01f) {
                                val glowAlpha = (0.35f * impactFactor).coerceIn(0f, 0.4f)
                                val baseGlowRadius = 12.dp.toPx() * impactFactor                                                                                    
                                if (baseGlowRadius > 0.5f && i in letterLayouts.indices) {
                                    try {
                                        drawIntoCanvas { canvas ->
                                            glowPaint.maskFilter = BlurMaskFilter(baseGlowRadius.coerceAtLeast(0.1f), BlurMaskFilter.Blur.NORMAL)
                                            glowPaint.color = expressiveAccent.copy(alpha = glowAlpha).toArgb()
                                            glowPaint.textSize = with(density) { lyricStyle.fontSize.toPx() }
                                            glowPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                            val textToDraw = letterLayouts[i].layoutInput.text.text
                                            val yPos = letterLayouts[i].firstBaseline
                                            canvas.nativeCanvas.drawText(textToDraw, 0f, yPos, glowPaint)
                                            glowPaint.maskFilter = null
                                        }
                                    } catch (e: Throwable) {
                                        glowPaint.maskFilter = null
                                    }
                                }
                            }
                        }
                        val baseAlpha = if (isWordSung || charLp > 0.99f) 1f else (focusedAlpha + (1f - focusedAlpha) * sungFactor)
                        drawText(textLayoutResult = letterLayouts[i], color = expressiveAccent.copy(alpha = if (wordIdx == -1) focusedAlpha else baseAlpha))
                        if (!isWordSung && charLp > 0f && charLp < 1f) {
                            val fXL = charBounds.width * charLp
                            val eW = (charBounds.width * 0.45f).coerceAtLeast(1f)
                            val sWL = (fXL - eW).coerceAtLeast(0f)
                            if (sWL > 0f) {
                                clipRect(left = 0f, top = 0f, right = sWL, bottom = charBounds.height) { drawText(textLayoutResult = letterLayouts[i], color = expressiveAccent) }
                            }
                            for (j in 0 until 12) {
                                val start = sWL + (j * eW / 12f)
                                val end = (sWL + ((j + 1) * eW / 12f) + 0.5f).coerceAtMost(fXL)
                                if (end > start) {
                                    clipRect(left = start, top = 0f, right = end, bottom = charBounds.height) { drawText(textLayoutResult = letterLayouts[i], color = expressiveAccent.copy(alpha = 1f - (j + 0.5f) / 12f)) }
                                }
                            }
                        }
                    }
                    lineCurrentPushes[lineIdx] += charBounds.width * (charScaleX - 1f)
                }
            }
        }
    }
}
