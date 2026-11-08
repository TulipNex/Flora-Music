import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Update FullScreenLyricsView signature and body
target = """@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenLyricsView(
    song: Song,
    isPlaying: Boolean,
    playbackPosition: Long,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onCloseLyrics: () -> Unit
) {"""

replacement = """@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenLyricsView(
    viewModel: MusicViewModel,
    song: Song,
    isPlaying: Boolean,
    playbackPosition: Long,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onCloseLyrics: () -> Unit
) {
    val lyricsAlignment by viewModel.lyricsAlignment.collectAsState()
    val lyricsActivePosition by viewModel.lyricsActivePosition.collectAsState()
    val lyricsActiveFontSize by viewModel.lyricsActiveFontSize.collectAsState()
    val lyricsOtherFontSize by viewModel.lyricsOtherFontSize.collectAsState()
    val lyricsSameFontSize by viewModel.lyricsSameFontSize.collectAsState()
    val lyricsUseDominantColor by viewModel.lyricsUseDominantColor.collectAsState()
    val lyricsActiveFontColor by viewModel.lyricsActiveFontColor.collectAsState()
    val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()

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

    val activeColorStr = if (lyricsUseDominantColor) "#FFFFFF" else lyricsActiveFontColor
    val otherColorStr = if (lyricsUseDominantColor) "#888888" else lyricsOtherFontColor

    val activeColor = try { Color(android.graphics.Color.parseColor(activeColorStr)) } catch(e: Exception) { Color.White }
    val otherColor = try { Color(android.graphics.Color.parseColor(otherColorStr)) } catch(e: Exception) { Color.White.copy(alpha = 0.5f) }"""

content = content.replace(target, replacement)

# Update the caller
call_target = """                FullScreenLyricsView(
                    song = song,
                    isPlaying = isPlaying,"""
call_replacement = """                FullScreenLyricsView(
                    viewModel = viewModel,
                    song = song,
                    isPlaying = isPlaying,"""
content = content.replace(call_target, call_replacement)

# Now, we need to update the rendering loop in FullScreenLyricsView
loop_target = """                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val verticalPad = (maxHeight / 2) - 30.dp

                            // Smoothly scroll to keep active item near the center
                            LaunchedEffect(activeIndexClean) {
                                if (isAutoScrollLocked && activeIndexClean >= 0) {
                                    // Since we pad exactly to the center, scrolling directly to the index centers it
                                    listState.animateScrollToItem(activeIndexClean)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = verticalPad, bottom = verticalPad),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(28.dp)
                            ) {
                            itemsIndexed(lines) { index, line ->
                                val currentFocusIndex = if (isAutoScrollLocked) activeIndexClean else centerIndex
                                val distance = kotlin.math.abs(index - currentFocusIndex)
                                
                                val isAudioActive = index == activeIndexClean
                                val isCenterFocus = index == currentFocusIndex

                                val alphaVal = when (distance) {
                                    0 -> 1.0f
                                    1 -> 0.75f
                                    2 -> 0.5f
                                    3 -> 0.25f
                                    else -> 0.0f
                                }
                                val scaleTarget = when (distance) {
                                    0 -> 1.1f
                                    1 -> 1.0f
                                    2 -> 0.9f
                                    3 -> 0.8f
                                    else -> 0.8f
                                }

                                val scale by animateFloatAsState(
                                    targetValue = scaleTarget,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                                    label = "LyricsScale_$index"
                                )

                                val animatedAlpha by animateFloatAsState(
                                    targetValue = alphaVal,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                                    label = "LyricsAlpha_$index"
                                )

                                val colorTarget = if (isAudioActive) EmeraldLight else Color.White

                                val colorState by animateColorAsState(
                                    targetValue = colorTarget,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                                    label = "LyricsColor_$index"
                                )
                                
                                val fontWeightTarget = if (isCenterFocus || isAudioActive) FontWeight.ExtraBold else FontWeight.Medium
                                Text(
                                    text = line.text,
                                    fontSize = 18.sp, // Static font size avoids layout jump pops
                                    fontWeight = fontWeightTarget,
                                    color = colorState,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 26.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSeekTo(line.timestampMs) }
                                        .padding(horizontal = 16.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = animatedAlpha
                                        }
                                )
                            }
                        }
                        }"""

loop_replacement = """                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val listTopPad = when (lyricsActivePosition) {
                                "TOP" -> 32.dp
                                "BOTTOM" -> maxHeight - 64.dp
                                else -> (maxHeight / 2) - 30.dp
                            }
                            val listBottomPad = when (lyricsActivePosition) {
                                "TOP" -> maxHeight - 64.dp
                                "BOTTOM" -> 32.dp
                                else -> (maxHeight / 2) - 30.dp
                            }

                            LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                                if (isAutoScrollLocked && activeIndexClean >= 0) {
                                    listState.animateScrollToItem(activeIndexClean)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = listTopPad, bottom = listBottomPad),
                                horizontalAlignment = colAlign,
                                verticalArrangement = Arrangement.spacedBy(28.dp)
                            ) {
                            itemsIndexed(lines) { index, line ->
                                val currentFocusIndex = if (isAutoScrollLocked) activeIndexClean else centerIndex
                                val distance = index - currentFocusIndex
                                
                                val isAudioActive = index == activeIndexClean
                                val isCenterFocus = index == currentFocusIndex

                                val alphaVal = when (lyricsActivePosition) {
                                    "TOP" -> {
                                        if (distance < 0) 0f
                                        else when (distance) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                    "BOTTOM" -> {
                                        if (distance > 0) 0f
                                        else when (-distance) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                    else -> {
                                        when (kotlin.math.abs(distance)) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                }

                                val scaleTarget = if (distance == 0) 1.1f else 1.0f

                                val scale by animateFloatAsState(
                                    targetValue = scaleTarget,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                                    label = "LyricsScale_$index"
                                )

                                val animatedAlpha by animateFloatAsState(
                                    targetValue = alphaVal,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                                    label = "LyricsAlpha_$index"
                                )

                                val colorTarget = if (isAudioActive) activeColor else otherColor

                                val colorState by animateColorAsState(
                                    targetValue = colorTarget,
                                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                                    label = "LyricsColor_$index"
                                )
                                
                                val fontWeightTarget = if (isCenterFocus || isAudioActive) FontWeight.ExtraBold else FontWeight.Medium
                                
                                val fontSizeTarget = if (lyricsSameFontSize) {
                                    lyricsActiveFontSize.sp
                                } else {
                                    if (isAudioActive) lyricsActiveFontSize.sp else lyricsOtherFontSize.sp
                                }

                                if (animatedAlpha > 0.01f) {
                                    Text(
                                        text = line.text,
                                        fontSize = fontSizeTarget, 
                                        fontWeight = fontWeightTarget,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = fontSizeTarget * 1.5f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                isAutoScrollLocked = true
                                                onSeekTo(line.timestampMs) 
                                            }
                                            .padding(horizontal = 16.dp)
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                                alpha = animatedAlpha
                                            }
                                    )
                                }
                            }
                        }
                        }"""

content = content.replace(loop_target, loop_replacement)

# We also need to update static lyrics alignment
static_target = """                                Text(
                                    text = parsedLyrics.text,
                                    fontSize = 18.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 32.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )"""

static_replacement = """                                Text(
                                    text = parsedLyrics.text,
                                    fontSize = 18.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 32.sp,
                                    textAlign = align,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )"""

content = content.replace(static_target, static_replacement)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
