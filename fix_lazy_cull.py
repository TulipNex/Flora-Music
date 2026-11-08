import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# For FullScreenLyricsView
old_loop1 = """                            lines.forEachIndexed { listIndex, line ->
                                val distance = kotlin.math.abs(listIndex - activeIndexClean)
                                val targetOffset = anchorY + (positions[listIndex] ?: ((listIndex - activeIndexClean).toFloat() * fallbackLineHeight))
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
                                Box("""
new_loop1 = """                            lines.forEachIndexed { listIndex, line ->
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
                                    Box("""
content = content.replace(old_loop1, new_loop1)

# For SynchronizedLyricsLayout
old_loop2 = """                lines.forEachIndexed { listIndex, line ->
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
                    Box("""
new_loop2 = """                lines.forEachIndexed { listIndex, line ->
                    val distance = kotlin.math.abs(listIndex - activeIndexClean)
                    val targetOffset = anchorY + positions.getOrDefault(listIndex, (listIndex - activeIndexClean) * fallbackLineHeight)
                    
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
                        val minScale = if (lyricsSameFontSize) (lyricsActiveFontSize / baseFontSize) else (lyricsOtherFontSize / baseFontSize)
                        val colorTarget = if (isAudioActive) activeColor else otherColor
                        val colorState by animateColorAsState(
                            targetValue = colorTarget,
                            animationSpec = tween(400, easing = LinearOutSlowInEasing),
                            label = "LyrColor_$listIndex"
                        )
                        Box("""
content = content.replace(old_loop2, new_loop2)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
