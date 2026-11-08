
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
                    if (wordIdx == 0) t = t.removePrefix("(")
                    if (wordIdx == effectiveWords.size - 1) t = t.removeSuffix(")")
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
            textMeasurer.measure(
                text = mainText,
                style = lyricStyle,
                constraints = Constraints(minWidth = maxWidthPx, maxWidth = maxWidthPx),
                softWrap = true
            )
        }
        
        // Each layout corresponds to one grapheme cluster (the visual unit),
        // not one codepoint. Fixes Devanagari/Bengali matra fragmentation.
        val letterLayouts = remember(mainText, lyricStyle) {
            graphemeClusters.map { cluster -> textMeasurer.measure(cluster, lyricStyle) }
        }
        
        val isRtlText = remember(mainText) { mainText.containsRtl() }
        
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(with(density) { layoutResult.size.height.toDp() })
            .graphicsLayer(
                clip = false,
                compositingStrategy = CompositingStrategy.Offscreen,
            )
        ) {
            if (mainText.isEmpty()) return@Canvas
            if (!isActiveLine) {
                drawText(layoutResult, color = lineColor)
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

                    drawText(layoutResult, color = lineColor.copy(alpha = focusedAlpha))

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
                                val bounds = layoutResult.getBoundingBox(charOffset)
                                left = minOf(left, bounds.left)
                                right = maxOf(right, bounds.right)
                                top = minOf(top, bounds.top)
                                bottom = maxOf(bottom, bounds.bottom)
                                found = true
                            }
                        }

                        if (found) {
                            if (isWordSung) {
                                clipRect(left = left, top = top, right = right, bottom = bottom) {
                                    drawText(layoutResult, color = expressiveAccent)
                                }
                            } else if (isWordActive && sungFactor > 0f) {
                                clipRect(left = left, top = top, right = right, bottom = bottom) {
                                    drawText(layoutResult, color = expressiveAccent.copy(alpha = focusedAlpha + (1f - focusedAlpha) * sungFactor))
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
                    val lineIdx = layoutResult.getLineForOffset(charOffset)
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
                            crescendoDeltaX = totalAtEnd * exp(-decay * pOut) * cos(freq * pOut * PI.toFloat()) * (1f - pOut)
                        } else if (groupWord.isLast) {
                            val base = groupWord.pos * baseScalePerSegment
                            val springPart = peakScale * (1f - exp(-decay * p) * cos(freq * p * PI.toFloat()) * (1f - p))
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
                        0.038f * sin(charLp * PI.toFloat()) * exp(-3f * charLp)
                    } else 0f

                    val charScaleX = 1f + (wobble * 0.025f) + crescendoDeltaX + (nudgeScale * 0.3f)
                    val charBounds = layoutResult.getBoundingBox(charOffset)
                    lineTotalPushes[lineIdx] += charBounds.width * (charScaleX - 1f)
                }

                // Main drawing loop: iterate over cluster indices so each visual
                // glyph (including multi-codepoint Devanagari clusters) is one unit.
                for (i in 0 until clusterCount) {
                    val charOffset = clusterCharOffsets[i]
                    val lineIdx = layoutResult.getLineForOffset(charOffset)
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
                            val springOut = totalAtEnd * exp(-decay * pOut) * cos(freq * pOut * PI.toFloat()) * (1f - pOut)
                            crescendoDeltaX = springOut
                            crescendoDeltaY = springOut
                        } else if (groupWord.isLast) {
                            val base = groupWord.pos * baseScalePerSegment
                            val springPart = peakScale * (1f - exp(-decay * p) * cos(freq * p * PI.toFloat()) * (1f - p))
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
                        nudgeStrength * sin(charLp * PI.toFloat()) * exp(-3f * charLp)
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
                                waveOffset = sin(wallTime * waveSpeed + phaseOffset) * waveHeight * waveFade
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
                                drawIntoCanvas { canvas ->
                                    glowPaint.maskFilter = BlurMaskFilter(baseGlowRadius, BlurMaskFilter.Blur.NORMAL)
                                    glowPaint.color = expressiveAccent.copy(alpha = glowAlpha).toArgb()
                                    glowPaint.textSize = lyricStyle.fontSize.toPx()
                                    glowPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    canvas.nativeCanvas.drawText(letterLayouts[i].layoutInput.text.text, 0f, letterLayouts[i].firstBaseline, glowPaint)
                                }
                            }
                        }
                        val baseAlpha = if (isWordSung || charLp > 0.99f) 1f else (focusedAlpha + (1f - focusedAlpha) * sungFactor)
                        drawText(letterLayouts[i], color = expressiveAccent.copy(alpha = if (wordIdx == -1) focusedAlpha else baseAlpha))
                        if (!isWordSung && charLp > 0f && charLp < 1f) {
                            val fXL = charBounds.width * charLp
                            val eW = (charBounds.width * 0.45f).coerceAtLeast(1f)
                            val sWL = (fXL - eW).coerceAtLeast(0f)
                            if (sWL > 0f) {
                                clipRect(left = 0f, top = 0f, right = sWL, bottom = charBounds.height) { drawText(letterLayouts[i], color = expressiveAccent) }
                            }
                            for (j in 0 until 12) {
                                val start = sWL + (j * eW / 12f)
                                val end = (sWL + ((j + 1) * eW / 12f) + 0.5f).coerceAtMost(fXL)
                                if (end > start) {
                                    clipRect(left = start, top = 0f, right = end, bottom = charBounds.height) { drawText(letterLayouts[i], color = expressiveAccent.copy(alpha = 1f - (j + 0.5f) / 12f)) }
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
