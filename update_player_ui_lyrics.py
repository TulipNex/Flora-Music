import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target1 = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()"""
replacement1 = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()
    val lyricsItemSpacing by viewModel.lyricsItemSpacing.collectAsState()
    val lyricsLineHeightMultiplier by viewModel.lyricsLineHeightMultiplier.collectAsState()"""
content = content.replace(target1, replacement1)

target2 = """                                verticalArrangement = Arrangement.spacedBy(32.dp)"""
replacement2 = """                                verticalArrangement = Arrangement.spacedBy(lyricsItemSpacing.dp)"""
content = content.replace(target2, replacement2)

target3 = """                                val spacingPx = 32.dp.toPx()"""
replacement3 = """                                val spacingPx = lyricsItemSpacing.dp.toPx()"""
content = content.replace(target3, replacement3)

target4 = """                                        lineHeight = (baseFontSize * 1.5f).sp,"""
replacement4 = """                                        lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,"""
content = content.replace(target4, replacement4)

target5 = """                    verticalArrangement = Arrangement.spacedBy(32.dp),"""
replacement5 = """                    verticalArrangement = Arrangement.spacedBy(lyricsItemSpacing.dp),"""
content = content.replace(target5, replacement5)

target6 = """                                val spacingPx = 16.dp.toPx()"""
replacement6 = """                                val spacingPx = lyricsItemSpacing.dp.toPx()"""
content = content.replace(target6, replacement6)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
