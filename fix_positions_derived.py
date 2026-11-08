import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Replace remember(itemHeights.toMap()...) with derivedStateOf in both places
old_positions1 = """                                val positions = remember(itemHeights.toMap(), activeIndexClean, lines) {
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
                                }"""
new_positions1 = """                                val positions by remember(activeIndexClean, lines) {
                                    derivedStateOf {
                                        val map = mutableMapOf<Int, Float>()
                                        if (lines.isEmpty()) map
                                        else {
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
                                    }
                                }"""
content = content.replace(old_positions1, new_positions1)

old_positions2 = """                val positions = remember(itemHeights.toMap(), activeIndexClean, lines) {
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
                }"""
new_positions2 = """                val positions by remember(activeIndexClean, lines) {
                    derivedStateOf {
                        val map = mutableMapOf<Int, Float>()
                        if (lines.isEmpty()) map
                        else {
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
                    }
                }"""
content = content.replace(old_positions2, new_positions2)

# Fix snapshotFlow { val h = itemHeights.toMap() } as well!
# It doesn't need toMap() either, it can just read from itemHeights.
old_flow1 = """                                    snapshotFlow { 
                                         val h = itemHeights.toMap()
                                        val windowStart = (activeIndexClean - 8).coerceAtLeast(0)
                                        val windowEnd = (activeIndexClean + 12).coerceAtMost(lines.size - 1)
                                        (windowStart..windowEnd).all { h.containsKey(it) } 
                                     }.first { it }"""
new_flow1 = """                                    snapshotFlow { 
                                        val windowStart = (activeIndexClean - 8).coerceAtLeast(0)
                                        val windowEnd = (activeIndexClean + 12).coerceAtMost(lines.size - 1)
                                        (windowStart..windowEnd).all { itemHeights.containsKey(it) } 
                                     }.first { it }"""
content = content.replace(old_flow1, new_flow1)

old_flow2 = """                        snapshotFlow { 
                             val h = itemHeights.toMap()
                            val windowStart = (activeIndexClean - 8).coerceAtLeast(0)
                            val windowEnd = (activeIndexClean + 12).coerceAtMost(lines.size - 1)
                            (windowStart..windowEnd).all { h.containsKey(it) } 
                         }.first { it }"""
new_flow2 = """                        snapshotFlow { 
                            val windowStart = (activeIndexClean - 8).coerceAtLeast(0)
                            val windowEnd = (activeIndexClean + 12).coerceAtMost(lines.size - 1)
                            (windowStart..windowEnd).all { itemHeights.containsKey(it) } 
                         }.first { it }"""
content = content.replace(old_flow2, new_flow2)


with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
