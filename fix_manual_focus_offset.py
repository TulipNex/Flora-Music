import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# First, remove the old centerIndex block from FullScreenLyricsView
target1 = """                        // 1. Calculate focused item during manual scroll
                        // animateScrollToItem places the item at offset 0 (start of content area, after padding).
                        val centerIndex by remember {
                            derivedStateOf {
                                val layoutInfo = listState.layoutInfo
                                val visibleItems = layoutInfo.visibleItemsInfo
                                if (visibleItems.isEmpty()) {
                                    activeIndexClean
                                } else {
                                    // The active item is the one closest to the start of the layout area (offset 0)
                                    val closest = visibleItems.minByOrNull { kotlin.math.abs(it.offset) }
                                    closest?.index ?: activeIndexClean
                                }
                            }
                        }"""

if target1 in content:
    content = content.replace(target1, "")

# Second, remove the old centerIndex block from SynchronizedLyricsLayout
target2 = """            // 1. Calculate focused item during manual scroll
            // animateScrollToItem places the item at offset 0 (start of content area, after padding).
            val centerIndex by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val visibleItems = layoutInfo.visibleItemsInfo
                    if (visibleItems.isEmpty()) {
                        activeIndexClean
                    } else {
                        // The active item is the one closest to the start of the layout area (offset 0)
                        val closest = visibleItems.minByOrNull { kotlin.math.abs(it.offset) }
                        closest?.index ?: activeIndexClean
                    }
                }
            }"""

if target2 in content:
    content = content.replace(target2, "")

# Now inject the new centerIndex block inside BoxWithConstraints in FullScreenLyricsView
target3 = """                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val listTopPad = when (lyricsActivePosition) {
                                "TOP" -> 32.dp
                                "BOTTOM" -> maxHeight - 64.dp
                                else -> (maxHeight / 2) - 30.dp
                            }
                            val listBottomPad = when (lyricsActivePosition) {
                                "TOP" -> maxHeight - 64.dp
                                "BOTTOM" -> 32.dp
                                else -> (maxHeight / 2) - 30.dp
                            }"""

replacement3 = """                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val listTopPad = when (lyricsActivePosition) {
                                "TOP" -> 32.dp
                                "BOTTOM" -> maxHeight - 64.dp
                                else -> (maxHeight / 2) - 30.dp
                            }
                            val listBottomPad = when (lyricsActivePosition) {
                                "TOP" -> maxHeight - 64.dp
                                "BOTTOM" -> 32.dp
                                else -> (maxHeight / 2) - 30.dp
                            }
                            
                            val density = LocalDensity.current
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
                            }"""

if target3 in content:
    content = content.replace(target3, replacement3)

# And similarly for SynchronizedLyricsLayout
target4 = """            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val listTopPad = when (lyricsActivePosition) {
                    "TOP" -> 16.dp
                    "BOTTOM" -> maxHeight - 48.dp
                    else -> (maxHeight / 2) - 30.dp
                }
                val listBottomPad = when (lyricsActivePosition) {
                    "TOP" -> maxHeight - 48.dp
                    "BOTTOM" -> 16.dp
                    else -> (maxHeight / 2) - 30.dp
                }"""

replacement4 = """            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val listTopPad = when (lyricsActivePosition) {
                    "TOP" -> 16.dp
                    "BOTTOM" -> maxHeight - 48.dp
                    else -> (maxHeight / 2) - 30.dp
                }
                val listBottomPad = when (lyricsActivePosition) {
                    "TOP" -> maxHeight - 48.dp
                    "BOTTOM" -> 16.dp
                    else -> (maxHeight / 2) - 30.dp
                }
                
                val density = LocalDensity.current
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
                }"""

if target4 in content:
    content = content.replace(target4, replacement4)
    
if "androidx.compose.ui.platform.LocalDensity" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.platform.LocalDensity")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

