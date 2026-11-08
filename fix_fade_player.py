import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Replace the Box we added earlier
old_box = """        // 2. CONTROLLER OR STANDALONE FULLSCREEN LYRICS
        Box(modifier = Modifier.fillMaxSize()) {
            val configuration = LocalConfiguration.current"""

new_box = """        // 2. CONTROLLER OR STANDALONE FULLSCREEN LYRICS
        val playerUiAlpha by animateFloatAsState(
            targetValue = if (showLyricsView) 0f else 1f, 
            animationSpec = tween(250, easing = LinearEasing),
            label = "playerUiAlpha"
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = playerUiAlpha }) {
                val configuration = LocalConfiguration.current"""

content = content.replace(old_box, new_box)

old_end = """            androidx.compose.animation.AnimatedVisibility(
                visible = showLyricsView,
                enter = slideInVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it } + fadeIn(animationSpec = tween(250)),
                exit = slideOutVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it } + fadeOut(animationSpec = tween(250)),
                modifier = Modifier.fillMaxSize()
            ) {
                FullScreenLyricsView("""

new_end = """            } // Close player UI Box

            androidx.compose.animation.AnimatedVisibility(
                visible = showLyricsView,
                enter = slideInVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it } + fadeIn(animationSpec = tween(250)),
                exit = slideOutVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it } + fadeOut(animationSpec = tween(250)),
                modifier = Modifier.fillMaxSize()
            ) {
                FullScreenLyricsView("""

content = content.replace(old_end, new_end)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
