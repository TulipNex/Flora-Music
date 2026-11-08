import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target_sync = """                    val currentFocusIndex = if (isAutoScrollLocked) activeIndexClean else centerIndex
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
                    val baseFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize.toFloat()
                    val targetScale = if (distance == 0) 1f else {
                        if (lyricsSameFontSize) (lyricsActiveFontSize / baseFontSize) else (lyricsOtherFontSize / baseFontSize)
                    }

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
                                transformOrigin = when (lyricsAlignment) {
                                    "LEFT" -> androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                    "RIGHT" -> androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)
                                    else -> androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                }
                            }
                    )"""

replacement_sync = """                    val isAudioActive = index == activeIndexClean
                    val baseFontSize = if (lyricsSameFontSize) (lyricsActiveFontSize * 1.05f) else lyricsActiveFontSize.toFloat()
                    val minScale = if (lyricsSameFontSize) (lyricsActiveFontSize / baseFontSize) else (lyricsOtherFontSize / baseFontSize)

                    val colorTarget = if (isAudioActive) activeColor else otherColor
                    val colorState by animateColorAsState(
                        targetValue = colorTarget,
                        animationSpec = tween(400, easing = LinearOutSlowInEasing),
                        label = "LyrColor_$index"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = lyricsMargin.dp)
                            .graphicsLayer {
                                val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                                val itemOffset = itemInfo?.offset?.toFloat() ?: ((index - activeIndexClean) * 100f)
                                val itemSize = itemInfo?.size?.toFloat() ?: 100f
                                val spacingPx = 16.dp.toPx()
                                
                                val continuousDist = itemOffset / (itemSize + spacingPx)
                                val absContinuousDist = kotlin.math.abs(continuousDist)
                                
                                val interpolatedScale = androidx.compose.ui.util.lerp(1f, minScale, absContinuousDist.coerceIn(0f, 1f))
                                scaleX = interpolatedScale
                                scaleY = interpolatedScale
                                
                                val rawAlpha = when (lyricsActivePosition) {
                                    "TOP" -> {
                                        if (continuousDist < -0.1f) 0f
                                        else kotlin.math.max(0f, 1f - (continuousDist * 0.25f))
                                    }
                                    "BOTTOM" -> {
                                        if (continuousDist > 0.1f) 0f
                                        else kotlin.math.max(0f, 1f - (-continuousDist * 0.25f))
                                    }
                                    else -> {
                                        kotlin.math.max(0f, 1f - (absContinuousDist * 0.25f))
                                    }
                                }
                                alpha = rawAlpha.coerceIn(0f, 1f)

                                transformOrigin = when (lyricsAlignment) {
                                    "LEFT" -> androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                    "RIGHT" -> androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)
                                    else -> androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                }
                            }
                    )"""

content = content.replace(target_sync, replacement_sync)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
