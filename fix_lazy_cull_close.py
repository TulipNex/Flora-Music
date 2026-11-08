import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# For FullScreenLyricsView
old_close1 = """                                        .padding(horizontal = lyricsMargin.dp)
                                        .onSizeChanged { itemHeights[listIndex] = it.height }
                                ) {
                                    
                                    Box(modifier = Modifier.fillMaxWidth().clickable {
                                        isAutoScrollLocked = true
                                        onSeekTo(line.startTimeMs)
                                    }) {
                                        if (line.words.isNotEmpty()) {
                                            val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
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
                                                    lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp
                                                ),
                                                lineColor = otherColor,
                                                expressiveAccent = activeColor,
                                                isBackground = line.isBackground,
                                                focusedAlpha = 0.5f,
                                                alignment = align
                                            )
                                        } else {
                                            val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
                                            Text(
                                                text = displayText,
                                                fontFamily = customFontFamily,
                                                fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorState,
                                                textAlign = align,
                                                lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }"""

new_close1 = """                                        .padding(horizontal = lyricsMargin.dp)
                                        .onSizeChanged { itemHeights[listIndex] = it.height }
                                ) {
                                    
                                    Box(modifier = Modifier.fillMaxWidth().clickable {
                                        isAutoScrollLocked = true
                                        onSeekTo(line.startTimeMs)
                                    }) {
                                        if (line.words.isNotEmpty()) {
                                            val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
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
                                                    lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp
                                                ),
                                                lineColor = otherColor,
                                                expressiveAccent = activeColor,
                                                isBackground = line.isBackground,
                                                focusedAlpha = 0.5f,
                                                alignment = align
                                            )
                                        } else {
                                            val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
                                            Text(
                                                text = displayText,
                                                fontFamily = customFontFamily,
                                                fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorState,
                                                textAlign = align,
                                                lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                                } // END isVisible IF
                            }
                        }
                    }"""

content = content.replace(old_close1, new_close1)

old_close2 = """                                    .padding(horizontal = lyricsMargin.dp)
                                    .onSizeChanged { itemHeights[listIndex] = it.height }
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().clickable {
                                    isAutoScrollLocked = true
                                    onSeekTo(line.startTimeMs)
                                }) {
                                    if (line.words.isNotEmpty()) {
                                        val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
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
                                                lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp
                                            ),
                                            lineColor = otherColor,
                                            expressiveAccent = activeColor,
                                            isBackground = line.isBackground,
                                            focusedAlpha = 0.5f,
                                            alignment = align
                                        )
                                    } else {
                                        val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
                                        Text(
                                            text = displayText,
                                            fontFamily = customFontFamily,
                                            fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorState,
                                            textAlign = align,
                                            lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }"""

new_close2 = """                                    .padding(horizontal = lyricsMargin.dp)
                                    .onSizeChanged { itemHeights[listIndex] = it.height }
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().clickable {
                                    isAutoScrollLocked = true
                                    onSeekTo(line.startTimeMs)
                                }) {
                                    if (line.words.isNotEmpty()) {
                                        val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
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
                                                lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp
                                            ),
                                            lineColor = otherColor,
                                            expressiveAccent = activeColor,
                                            isBackground = line.isBackground,
                                            focusedAlpha = 0.5f,
                                            alignment = align
                                        )
                                    } else {
                                        val displayText = if (line.isBackground) line.text.removePrefix("(").removeSuffix(")") else line.text
                                        Text(
                                            text = displayText,
                                            fontFamily = customFontFamily,
                                            fontSize = if (line.isBackground) (baseFontSize * 0.75f).sp else baseFontSize.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorState,
                                            textAlign = align,
                                            lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            } // END isVisible IF
                        }
                    }
                }
            }"""

content = content.replace(old_close2, new_close2)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
