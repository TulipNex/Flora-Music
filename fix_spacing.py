with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Fix spacing going upwards in FullScreenLyricsView (around line 1445)
old_upward_full = '''                                for (i in activeIndexClean - 1 downTo 0) {
                                    val height = itemHeights[i]?.toFloat() ?: fallbackLineHeight
                                    val line = lines.getOrNull(i)
                                    val noGap = line?.isBackground == true
                                    currentY -= (height + if (noGap) 0f else with(density) { lyricsItemSpacing.dp.toPx() })
                                    map[i] = currentY
                                }'''

new_upward_full = '''                                for (i in activeIndexClean - 1 downTo 0) {
                                    val height = itemHeights[i]?.toFloat() ?: fallbackLineHeight
                                    val lineBelow = lines.getOrNull(i + 1)
                                    val noGap = lineBelow?.isBackground == true
                                    currentY -= (height + if (noGap) 0f else with(density) { lyricsItemSpacing.dp.toPx() })
                                    map[i] = currentY
                                }'''
content = content.replace(old_upward_full, new_upward_full)

# Fix spacing going upwards in SynchronizedLyricsLayout (around line 1815)
old_upward_sync = '''                    for (i in activeIndexClean - 1 downTo 0) {
                        val height = itemHeights[i]?.toFloat() ?: fallbackLineHeight
                        val line = lines.getOrNull(i)
                        val noGap = line?.isBackground == true
                        currentY -= (height + if (noGap) 0f else with(density) { lyricsItemSpacing.dp.toPx() })
                        map[i] = currentY
                    }'''
new_upward_sync = '''                    for (i in activeIndexClean - 1 downTo 0) {
                        val height = itemHeights[i]?.toFloat() ?: fallbackLineHeight
                        val lineBelow = lines.getOrNull(i + 1)
                        val noGap = lineBelow?.isBackground == true
                        currentY -= (height + if (noGap) 0f else with(density) { lyricsItemSpacing.dp.toPx() })
                        map[i] = currentY
                    }'''
content = content.replace(old_upward_sync, new_upward_sync)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
