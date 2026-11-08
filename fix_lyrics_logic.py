import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target1 = """                            val density = LocalDensity.current
                            val targetOffsetPx = remember(listTopPad, density) {
                                with(density) { listTopPad.toPx() }
                            }

                            val centerIndex by remember {
                                derivedStateOf {
                                    val layoutInfo = listState.layoutInfo
                                    val visibleItems = layoutInfo.visibleItemsInfo
                                    if (visibleItems.isEmpty()) {
                                        activeIndexClean
                                    } else {
                                        val closest = visibleItems.minByOrNull { kotlin.math.abs(it.offset - targetOffsetPx) }
                                        closest?.index ?: activeIndexClean
                                    }
                                }
                            }

                            // Smoothly scroll to keep active item near the focus position
                            LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                                if (isAutoScrollLocked && activeIndexClean >= 0) {
                                    listState.animateScrollToItem(activeIndexClean)
                                }
                            }"""

replacement1 = """                            val density = LocalDensity.current

                            val centerIndex by remember {
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
                            }

                            // Smoothly scroll to keep active item near the focus position
                            LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                                if (isAutoScrollLocked && activeIndexClean >= 0) {
                                    val viewportHeight = listState.layoutInfo.viewportSize.height
                                    val targetOffset = when (lyricsActivePosition) {
                                        "TOP" -> with(density) { 32.dp.toPx() }
                                        "BOTTOM" -> viewportHeight - with(density) { 64.dp.toPx() }
                                        else -> viewportHeight / 2f
                                    }
                                    listState.animateScrollToItem(
                                        index = activeIndexClean,
                                        scrollOffset = -targetOffset.toInt()
                                    )
                                }
                            }"""

target2 = """                val density = LocalDensity.current
                val targetOffsetPx = remember(listTopPad, density) {
                    with(density) { listTopPad.toPx() }
                }

                val centerIndex by remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isEmpty()) {
                            activeIndexClean
                        } else {
                            val closest = visibleItems.minByOrNull { kotlin.math.abs(it.offset - targetOffsetPx) }
                            closest?.index ?: activeIndexClean
                        }
                    }
                }

                // Smoothly scroll to keep active item near the center
                LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                    if (isAutoScrollLocked && activeIndexClean >= 0) {
                        listState.animateScrollToItem(activeIndexClean)
                    }
                }"""

replacement2 = """                val density = LocalDensity.current

                val centerIndex by remember {
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
                }

                // Smoothly scroll to keep active item near the center
                LaunchedEffect(activeIndexClean, lyricsActivePosition) {
                    if (isAutoScrollLocked && activeIndexClean >= 0) {
                        val viewportHeight = listState.layoutInfo.viewportSize.height
                        val targetOffset = when (lyricsActivePosition) {
                            "TOP" -> with(density) { 16.dp.toPx() }
                            "BOTTOM" -> viewportHeight - with(density) { 48.dp.toPx() }
                            else -> viewportHeight / 2f
                        }
                        listState.animateScrollToItem(
                            index = activeIndexClean,
                            scrollOffset = -targetOffset.toInt()
                        )
                    }
                }"""

if target1 in content:
    content = content.replace(target1, replacement1)
    print("Replaced target 1")
else:
    print("Target 1 not found")

if target2 in content:
    content = content.replace(target2, replacement2)
    print("Replaced target 2")
else:
    print("Target 2 not found")
    
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

