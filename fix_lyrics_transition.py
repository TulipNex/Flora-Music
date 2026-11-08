with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

old_block = """        // 2. CONTROLLER OR STANDALONE FULLSCREEN LYRICS
        AnimatedContent(
            targetState = showLyricsView,
            transitionSpec = {
                val duration = 250
                if (targetState) {
                    (slideInVertically(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { it } +
                            fadeIn(animationSpec = tween(duration))) togetherWith
                            fadeOut(animationSpec = tween(duration))
                } else {
                    fadeIn(animationSpec = tween(duration)) togetherWith
                            (slideOutVertically(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { it } +
                                    fadeOut(animationSpec = tween(duration)))
                }
            },
            label = "LyricsTransition"
        ) { isLyrics ->
            if (isLyrics) {
                FullScreenLyricsView(
                    viewModel = viewModel,
                    song = song,
                    isPlaying = isPlaying,
                    playbackPosition = playbackPosition,
                    artBitmap = artBitmap,
                    artDominantColor = artDominantColor,
                    onPlayPauseClick = { viewModel.playerController.togglePlay() },
                    onSeekTo = { viewModel.playerController.seekTo(it) },
                    onCloseLyrics = { showLyricsView = false }
                )
            } else {
            val configuration = LocalConfiguration.current"""

new_block = """        // 2. CONTROLLER OR STANDALONE FULLSCREEN LYRICS
        Box(modifier = Modifier.fillMaxSize()) {
            val configuration = LocalConfiguration.current"""

content = content.replace(old_block, new_block)

old_end = """                        }
                    }
                }
            }
        }
    }
}

// standalone fullscreen standby lyrics view with interactive click to seek"""

new_end = """                        }
                    }
                }
            }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = showLyricsView,
                enter = slideInVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it } + fadeIn(animationSpec = tween(250)),
                exit = slideOutVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it } + fadeOut(animationSpec = tween(250)),
                modifier = Modifier.fillMaxSize()
            ) {
                FullScreenLyricsView(
                    viewModel = viewModel,
                    song = song,
                    isPlaying = isPlaying,
                    playbackPosition = playbackPosition,
                    artBitmap = artBitmap,
                    artDominantColor = artDominantColor,
                    onPlayPauseClick = { viewModel.playerController.togglePlay() },
                    onSeekTo = { viewModel.playerController.seekTo(it) },
                    onCloseLyrics = { showLyricsView = false }
                )
            }
        }
    }
}

// standalone fullscreen standby lyrics view with interactive click to seek"""
content = content.replace(old_end, new_end)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
