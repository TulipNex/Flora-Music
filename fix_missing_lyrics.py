import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

old_end = """            }
        }
        }
    }

    // Preset modals"""

new_end = """            }
        }
        }
    }

            androidx.compose.animation.AnimatedVisibility(
                visible = showLyricsView,
                enter = androidx.compose.animation.slideInVertically(animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)),
                exit = androidx.compose.animation.slideOutVertically(animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)),
                modifier = androidx.compose.ui.Modifier.fillMaxSize()
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
        } // Close outer Box

    // Preset modals"""

content = content.replace(old_end, new_end)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
