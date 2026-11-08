import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """
                                    val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
                                        if (line.words.isNotEmpty() && isAudioActive) {
                                            line.words.forEachIndexed { i, word ->
                                                val isWordActive = playbackPosition >= word.startTimeMs
                                                val c = if (isWordActive) colorState else otherColor
                                                withStyle(androidx.compose.ui.text.SpanStyle(color = c)) {
                                                    append(word.text)
                                                }
                                                if (i < line.words.lastIndex) {
                                                    val nextTime = line.words[i+1].startTimeMs
                                                    val cSpace = if (playbackPosition >= nextTime) colorState else otherColor
                                                    withStyle(androidx.compose.ui.text.SpanStyle(color = cSpace)) {
                                                        append(" ")
                                                    }
                                                }
                                            }
                                        } else {
                                            append(line.text)
                                        }
                                    }
                                    
                                    Text(
                                        text = annotatedString,
                                        fontFamily = customFontFamily,
                                        fontSize = (if (line.isBackground) baseFontSize * 0.75f else baseFontSize).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                 isAutoScrollLocked = true
                                                onSeekTo(line.timestampMs)
                                            }
                                    )
"""

replacement = """
                                    Box(modifier = Modifier.fillMaxWidth().clickable {
                                        isAutoScrollLocked = true
                                        onSeekTo(line.timestampMs)
                                    }) {
                                        if (line.words.isNotEmpty()) {
                                            FloraWordLevelLyrics(
                                                mainText = line.text,
                                                words = line.words,
                                                isActiveLine = isAudioActive,
                                                playbackPosition = playbackPosition,
                                                isPlaying = isPlaying,
                                                lyricStyle = TextStyle(
                                                    fontFamily = customFontFamily,
                                                    fontSize = (if (line.isBackground) baseFontSize * 0.75f else baseFontSize).sp,
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
                                            Text(
                                                text = line.text,
                                                fontFamily = customFontFamily,
                                                fontSize = (if (line.isBackground) baseFontSize * 0.75f else baseFontSize).sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorState,
                                                textAlign = align,
                                                lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
"""

if target.strip() in content:
    print("Found exact match!")
    # Wait, the indentation might be slightly different. Let's do a substring replace
else:
    print("Exact match not found, using regex...")
    
content = content.replace(target, replacement)
# Fallback regex if replace failed
if "val annotatedString = androidx.compose.ui.text.buildAnnotatedString" in content:
    # try replacing with regex
    import re
    content = re.sub(r'val annotatedString = androidx.compose.ui.text.buildAnnotatedString \{.*?Text\([^)]*\)', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
