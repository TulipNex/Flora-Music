import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target1 = """                        // 1. Calculate visually centered item
                        val centerIndex by remember {
                            derivedStateOf {
                                val layoutInfo = listState.layoutInfo
                                val visibleItems = layoutInfo.visibleItemsInfo
                                if (visibleItems.isEmpty()) {
                                    activeIndexClean
                                } else {
                                    val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                                    val closest = visibleItems.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                                    closest?.index ?: activeIndexClean
                                }
                            }
                        }"""

replacement1 = """                        // 1. Calculate focused item during manual scroll
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

target2 = """            // 1. Calculate visually centered item
            val centerIndex by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val visibleItems = layoutInfo.visibleItemsInfo
                    if (visibleItems.isEmpty()) {
                        activeIndexClean
                    } else {
                        val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                        val closest = visibleItems.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                        closest?.index ?: activeIndexClean
                    }
                }
            }"""

replacement2 = """            // 1. Calculate focused item during manual scroll
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
    content = content.replace(target1, replacement1)
    print("Replaced 1")

if target2 in content:
    content = content.replace(target2, replacement2)
    print("Replaced 2")
    
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

