import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Add lyricsMargin to SynchronizedLyricsLayout
sync_decl_target = """    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()"""
sync_decl_replacement = """    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()
    val lyricsMargin by viewModel.lyricsMargin.collectAsState()"""
content = content.replace(sync_decl_target, sync_decl_replacement)

# FullScreenLyricsView static text:
static_1_target = """                                    textAlign = align,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                    is LyricState.Synced -> {"""
static_1_replacement = """                                    textAlign = align,
                                    modifier = Modifier.padding(horizontal = lyricsMargin.dp)
                                )
                            }
                        }
                    }
                    is LyricState.Synced -> {"""
content = content.replace(static_1_target, static_1_replacement)

# FullScreenLyricsView synced box:
synced_1_target = """                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .graphicsLayer {"""
synced_1_replacement = """                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = lyricsMargin.dp)
                                        .graphicsLayer {"""
content = content.replace(synced_1_target, synced_1_replacement)

# SynchronizedLyricsLayout static text:
static_2_target = """                        textAlign = align,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
        is LyricState.Synced -> {"""
static_2_replacement = """                        textAlign = align,
                        modifier = Modifier.padding(horizontal = lyricsMargin.dp)
                    )
                }
            }
        }
        is LyricState.Synced -> {"""
content = content.replace(static_2_target, static_2_replacement)

# SynchronizedLyricsLayout synced box:
# Actually wait, the `synced_1_target` is not unique, there are two of them, so it might replace both! Let's check if the second replacement string is what we want.
# `synced_1_target` is exactly the same for both FullScreenLyricsView and SynchronizedLyricsLayout.
# So `content = content.replace(synced_1_target, synced_1_replacement)` will replace both instances, which is exactly what we want!

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
