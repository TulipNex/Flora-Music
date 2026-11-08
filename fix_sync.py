import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Let's find the SynchronizedLyricsLayout definition and add isPlaying
# Then pass isPlaying down

old_sig = """fun SynchronizedLyricsLayout(
    viewModel: MusicViewModel,
    song: Song,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit
)"""

new_sig = """fun SynchronizedLyricsLayout(
    viewModel: MusicViewModel,
    song: Song,
    currentPositionMs: Long,
    isPlaying: Boolean,
    onSeekTo: (Long) -> Unit
)"""

content = content.replace(old_sig, new_sig)

# Inside SynchronizedLyricsLayout, I need to replace playbackPosition = playbackPosition with playbackPosition = currentPositionMs
# But wait, both occurrences were replaced with `playbackPosition = playbackPosition` and `isPlaying = isPlaying`

# I can just write a smart regex that finds the invocation inside SynchronizedLyricsLayout.
# Since SynchronizedLyricsLayout is after FullScreenLyricsView, I can just find the second occurrence.

parts = content.split('FloraWordLevelLyrics(')
if len(parts) == 4: # 1 before, 2 invocations, 1 definition
    # parts[1] is inside FullScreenLyricsView
    # parts[2] is inside SynchronizedLyricsLayout
    parts[2] = parts[2].replace('playbackPosition = playbackPosition,', 'playbackPosition = currentPositionMs,')
    content = 'FloraWordLevelLyrics('.join(parts)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
