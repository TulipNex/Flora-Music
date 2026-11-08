with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

none_block = """                when (val state = parsedLyrics) {
                    is LyricState.None -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Lirik tidak tersedia.",
                                fontSize = 18.sp,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }"""
                    
loading_block = """                when (val state = parsedLyrics) {
                    is LyricState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    is LyricState.None -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Lirik tidak tersedia.",
                                fontSize = 18.sp,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }"""

content = content.replace(none_block, loading_block)

none_block2 = """    when (val state = parsedLyrics) {
        is LyricState.None -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Lirik tidak tersedia.",
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }"""
        
loading_block2 = """    when (val state = parsedLyrics) {
        is LyricState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        is LyricState.None -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Lirik tidak tersedia.",
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }"""
        
content = content.replace(none_block2, loading_block2)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
