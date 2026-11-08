import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Add lyricsMargin declaration
decl_target = """    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()"""
decl_replacement = """    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()
    val lyricsMargin by viewModel.lyricsMargin.collectAsState()"""
content = content.replace(decl_target, decl_replacement)

# Now, we need to update the padding of the lyrics lines and the static text.
# The synchronized lyrics have this:
'''
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp)
'''
# There are 2 places with padding(horizontal = 32.dp) inside FullScreenLyricsView or SynchronizedLyricsLayout.
# Let's see what exactly is there.
