import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Fix FullScreenLyricsView centerIndex
target_center_1 = """                            val centerIndex by remember {
                                derivedStateOf {
                                    val layoutInfo = listState.layoutInfo
                                    val visibleItems = layoutInfo.visibleItemsInfo
                                    if (visibleItems.isEmpty()) {
                                        activeIndexClean
                                    } else {
                                        val viewportHeight = layoutInfo.viewportSize.height
                                        val targetOffset = when (lyricsActivePosition) {
                                            "TOP" -> with(density) { 32.dp.toPx() }
                                            "BOTTOM" -> viewportHeight - with(density) { 64.dp.toPx() }
                                            else -> viewportHeight / 2f
                                        }
                                        val closest = visibleItems.minByOrNull { kotlin.math.abs((it.offset + (it.size / 2f)) - targetOffset) }
                                        closest?.index ?: activeIndexClean
                                    }
                                }
                            }"""

replacement_center_1 = """                            val centerIndex by remember {
                                derivedStateOf {
                                    val layoutInfo = listState.layoutInfo
                                    val visibleItems = layoutInfo.visibleItemsInfo
                                    if (visibleItems.isEmpty()) {
                                        activeIndexClean
                                    } else {
                                        val closest = visibleItems.minByOrNull { kotlin.math.abs(it.offset) }
                                        closest?.index ?: activeIndexClean
                                    }
                                }
                            }"""

content = content.replace(target_center_1, replacement_center_1)

# Fix SynchronizedLyricsLayout centerIndex
target_center_2 = """                val centerIndex by remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isEmpty()) {
                            activeIndexClean
                        } else {
                            val viewportHeight = layoutInfo.viewportSize.height
                            val targetOffset = when (lyricsActivePosition) {
                                "TOP" -> with(density) { 16.dp.toPx() }
                                "BOTTOM" -> viewportHeight - with(density) { 48.dp.toPx() }
                                else -> viewportHeight / 2f
                            }
                            val closest = visibleItems.minByOrNull { kotlin.math.abs((it.offset + (it.size / 2f)) - targetOffset) }
                            closest?.index ?: activeIndexClean
                        }
                    }
                }"""

replacement_center_2 = """                val centerIndex by remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isEmpty()) {
                            activeIndexClean
                        } else {
                            val closest = visibleItems.minByOrNull { kotlin.math.abs(it.offset) }
                            closest?.index ?: activeIndexClean
                        }
                    }
                }"""
content = content.replace(target_center_2, replacement_center_2)

# Also fix the LaunchedEffect for auto-scroll since I added viewportHeight calculations there too
target_scroll_1 = """                            // Smoothly scroll to keep active item near the focus position
                            LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                                if (isAutoScrollLocked && activeIndexClean >= 0) {
                                    val viewportHeight = listState.layoutInfo.viewportSize.height
                                    val targetOffset = when (lyricsActivePosition) {
                                        "TOP" -> with(density) { 32.dp.toPx() }
                                        "BOTTOM" -> viewportHeight - with(density) { 64.dp.toPx() }
                                        else -> viewportHeight / 2f
                                    }
                                    listState.animateScrollToItem(
                                        index = activeIndexClean
                                    )
                                }
                            }"""
                            
replacement_scroll_1 = """                            // Smoothly scroll to keep active item near the focus position
                            LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                                if (isAutoScrollLocked && activeIndexClean >= 0) {
                                    listState.animateScrollToItem(activeIndexClean)
                                }
                            }"""
content = content.replace(target_scroll_1, replacement_scroll_1)

target_scroll_2 = """                // Smoothly scroll to keep active item near the center
                LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                    if (isAutoScrollLocked && activeIndexClean >= 0) {
                        val viewportHeight = listState.layoutInfo.viewportSize.height
                        val targetOffset = when (lyricsActivePosition) {
                            "TOP" -> with(density) { 16.dp.toPx() }
                            "BOTTOM" -> viewportHeight - with(density) { 48.dp.toPx() }
                            else -> viewportHeight / 2f
                        }
                        listState.animateScrollToItem(
                            index = activeIndexClean
                        )
                    }
                }"""
                
replacement_scroll_2 = """                // Smoothly scroll to keep active item near the center
                LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                    if (isAutoScrollLocked && activeIndexClean >= 0) {
                        listState.animateScrollToItem(activeIndexClean)
                    }
                }"""
content = content.replace(target_scroll_2, replacement_scroll_2)

# Fix itemsIndexed in FullScreenLyricsView
target_items_1 = """                                val targetFontSize = if (lyricsSameFontSize) {
                                    if (distance == 0) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize
                                } else {
                                    if (distance == 0) lyricsActiveFontSize else lyricsOtherFontSize
                                }

                                val animatedFontSize by animateFloatAsState(
                                    targetValue = targetFontSize,
                                    animationSpec = tween(600, easing = LinearOutSlowInEasing),
                                    label = "LyricsFontSize_$index"
                                )

                                val animatedAlpha by animateFloatAsState(
                                    targetValue = alphaVal,
                                    animationSpec = tween(600, easing = LinearOutSlowInEasing),
                                    label = "LyricsAlpha_$index"
                                )

                                val colorTarget = if (isAudioActive) activeColor else otherColor

                                val colorState by animateColorAsState(
                                    targetValue = colorTarget,
                                    animationSpec = tween(600, easing = LinearOutSlowInEasing),
                                    label = "LyricsColor_$index"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = lyricsMargin.dp)
                                        .graphicsLayer {
                                            alpha = animatedAlpha
                                        }
                                ) {
                                    Text(
                                        text = line.text,
                                        fontSize = animatedFontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = (animatedFontSize * 1.5f).sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                isAutoScrollLocked = true
                                                onSeekTo(line.timestampMs) 
                                            }
                                    )
                                }"""

replacement_items_1 = """                                val baseFontSize = if (lyricsSameFontSize) lyricsActiveFontSize else lyricsOtherFontSize
                                val maxFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize
                                val targetScale = if (distance == 0) (maxFontSize / baseFontSize) else 1f

                                val animatedScale by animateFloatAsState(
                                    targetValue = targetScale,
                                    animationSpec = tween(600, easing = LinearOutSlowInEasing),
                                    label = "LyricsScale_$index"
                                )

                                val animatedAlpha by animateFloatAsState(
                                    targetValue = alphaVal,
                                    animationSpec = tween(600, easing = LinearOutSlowInEasing),
                                    label = "LyricsAlpha_$index"
                                )

                                val colorTarget = if (isAudioActive) activeColor else otherColor

                                val colorState by animateColorAsState(
                                    targetValue = colorTarget,
                                    animationSpec = tween(600, easing = LinearOutSlowInEasing),
                                    label = "LyricsColor_$index"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = lyricsMargin.dp)
                                        .graphicsLayer {
                                            alpha = animatedAlpha
                                            scaleX = animatedScale
                                            scaleY = animatedScale
                                        }
                                ) {
                                    Text(
                                        text = line.text,
                                        fontSize = baseFontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = (baseFontSize * 1.5f).sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                isAutoScrollLocked = true
                                                onSeekTo(line.timestampMs) 
                                            }
                                    )
                                }"""
content = content.replace(target_items_1, replacement_items_1)

# Fix itemsIndexed in SynchronizedLyricsLayout
target_items_2 = """                    val targetFontSize = if (lyricsSameFontSize) {
                        if (distance == 0) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize
                    } else {
                        if (distance == 0) lyricsActiveFontSize else lyricsOtherFontSize
                    }

                    val animatedFontSize by animateFloatAsState(
                        targetValue = targetFontSize,
                        animationSpec = tween(600, easing = LinearOutSlowInEasing),
                        label = "LyrFontSize_$index"
                    )

                    val animatedAlpha by animateFloatAsState(
                        targetValue = alphaVal,
                        animationSpec = tween(600, easing = LinearOutSlowInEasing),
                        label = "LyrAlpha_$index"
                    )

                    val colorTarget = if (isAudioActive) activeColor else otherColor

                    val colorState by animateColorAsState(
                        targetValue = colorTarget,
                        animationSpec = tween(600, easing = LinearOutSlowInEasing),
                        label = "LyrColor_$index"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = lyricsMargin.dp)
                            .graphicsLayer {
                                alpha = animatedAlpha
                            }
                    ) {
                        Text(
                            text = line.text,
                            fontSize = animatedFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorState,
                            textAlign = align,
                            lineHeight = (animatedFontSize * 1.5f).sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    isAutoScrollLocked = true
                                    onSeekTo(line.timestampMs) 
                                }
                        )
                    }"""

replacement_items_2 = """                    val baseFontSize = if (lyricsSameFontSize) lyricsActiveFontSize else lyricsOtherFontSize
                    val maxFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize
                    val targetScale = if (distance == 0) (maxFontSize / baseFontSize) else 1f

                    val animatedScale by animateFloatAsState(
                        targetValue = targetScale,
                        animationSpec = tween(600, easing = LinearOutSlowInEasing),
                        label = "LyrScale_$index"
                    )

                    val animatedAlpha by animateFloatAsState(
                        targetValue = alphaVal,
                        animationSpec = tween(600, easing = LinearOutSlowInEasing),
                        label = "LyrAlpha_$index"
                    )

                    val colorTarget = if (isAudioActive) activeColor else otherColor

                    val colorState by animateColorAsState(
                        targetValue = colorTarget,
                        animationSpec = tween(600, easing = LinearOutSlowInEasing),
                        label = "LyrColor_$index"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = lyricsMargin.dp)
                            .graphicsLayer {
                                alpha = animatedAlpha
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }
                    ) {
                        Text(
                            text = line.text,
                            fontSize = baseFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorState,
                            textAlign = align,
                            lineHeight = (baseFontSize * 1.5f).sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    isAutoScrollLocked = true
                                    onSeekTo(line.timestampMs) 
                                }
                        )
                    }"""
content = content.replace(target_items_2, replacement_items_2)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

