import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Add artDominantColor calculation after artBitmap
target_art_bitmap = """    val artBitmap = remember(songArtBytes) {
        songArtBytes?.let {
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            } catch (e: Exception) {
                null
            }
        }
    }"""

replacement_art_bitmap = target_art_bitmap + """
    
    var artDominantColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
    LaunchedEffect(artBitmap) {
        if (artBitmap != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val palette = androidx.palette.graphics.Palette.from(artBitmap).generate()
                    val vibrant = palette.vibrantSwatch
                    val lightVibrant = palette.lightVibrantSwatch
                    val dominant = palette.dominantSwatch
                    
                    val colorInt = lightVibrant?.rgb ?: vibrant?.rgb ?: dominant?.rgb ?: android.graphics.Color.WHITE
                    artDominantColor = androidx.compose.ui.graphics.Color(colorInt)
                } catch (e: Exception) {
                    artDominantColor = androidx.compose.ui.graphics.Color.White
                }
            }
        } else {
            artDominantColor = null
        }
    }"""

if target_art_bitmap in content:
    content = content.replace(target_art_bitmap, replacement_art_bitmap)
    print("Added artDominantColor calculation")
else:
    print("Could not find artBitmap target")

# Now update the activeColorStr and otherColorStr usage (Line 1121 and 1431)
# There are two places.
target_color_1 = """    val activeColorStr = if (lyricsUseDominantColor) "#FFFFFF" else lyricsActiveFontColor
    val otherColorStr = if (lyricsUseDominantColor) "#888888" else lyricsOtherFontColor

    val activeColor = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(activeColorStr)) } catch(e: Exception) { androidx.compose.ui.graphics.Color.White }
    val otherColor = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(otherColorStr)) } catch(e: Exception) { androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f) }"""

replacement_color_1 = """    val activeColor = if (lyricsUseDominantColor) {
        artDominantColor ?: androidx.compose.ui.graphics.Color.White
    } else {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(lyricsActiveFontColor)) } catch(e: Exception) { androidx.compose.ui.graphics.Color.White }
    }
    
    val otherColor = if (lyricsUseDominantColor) {
        (artDominantColor ?: androidx.compose.ui.graphics.Color.White).copy(alpha = 0.5f)
    } else {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(lyricsOtherFontColor)) } catch(e: Exception) { androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f) }
    }"""

if target_color_1 in content:
    content = content.replace(target_color_1, replacement_color_1)
    print("Replaced color logic")
else:
    print("Could not find color logic target")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
