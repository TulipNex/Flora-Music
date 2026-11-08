import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# For FullScreenLyricsView
target_1 = """                                val scaleTarget = if (lyricsSameFontSize) {
                                    if (distance == 0) 1.05f else 1.0f
                                } else {
                                    if (distance == 0) (lyricsActiveFontSize / lyricsOtherFontSize.coerceAtLeast(1f)) else 1.0f
                                }

                                val scale by animateFloatAsState(
                                    targetValue = scaleTarget,
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
                                
                                val staticFontSize = if (lyricsSameFontSize) lyricsActiveFontSize.sp else lyricsOtherFontSize.sp
                                
                                // Fix layout jump pops by ensuring layout size NEVER changes dynamically
                                // Use graphicsLayer scale for size changes, static fontWeight, and constant modifiers.
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = lyricsMargin.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = animatedAlpha
                                        }
                                ) {
                                    Text(
                                        text = line.text,
                                        fontSize = staticFontSize, // Static font size avoids layout jump pops
                                        fontWeight = FontWeight.Bold, // Static font weight avoids layout jump pops
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = staticFontSize * 1.5f,"""

replacement_1 = """                                val targetFontSize = if (lyricsSameFontSize) {
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
                                        lineHeight = (animatedFontSize * 1.5f).sp,"""

content = content.replace(target_1, replacement_1)

# Now for SynchronizedLyricsLayout
target_2 = """                    val scaleTarget = if (lyricsSameFontSize) {
                        if (distance == 0) 1.05f else 1.0f
                    } else {
                        if (distance == 0) (lyricsActiveFontSize / lyricsOtherFontSize.coerceAtLeast(1f)) else 1.0f
                    }

                    val scale by animateFloatAsState(
                        targetValue = scaleTarget,
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

                    val staticFontSize = if (lyricsSameFontSize) lyricsActiveFontSize.sp else lyricsOtherFontSize.sp

                    // Fix layout jump pops
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                alpha = animatedAlpha
                            }
                    ) {
                        Text(
                            text = line.text,
                            fontSize = staticFontSize,
                            fontWeight = FontWeight.Bold,
                            color = colorState,
                            textAlign = align,
                            lineHeight = staticFontSize * 1.5f,"""

replacement_2 = """                    val targetFontSize = if (lyricsSameFontSize) {
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
                            lineHeight = (animatedFontSize * 1.5f).sp,"""

content = content.replace(target_2, replacement_2)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

