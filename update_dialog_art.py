import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """    val scrollState = rememberScrollState()"""

replacement = """    val contextLocal = LocalContext.current
    var songArtBytes by remember(song.path, customCoverUri) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.path, customCoverUri) {
        songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (customCoverUri != null) {
                try {
                    contextLocal.contentResolver.openInputStream(customCoverUri!!)?.use { it.readBytes() }
                } catch(e: Exception) {
                    com.example.utils.MusicScanner.getArtworkBytes(song.path)
                }
            } else {
                com.example.utils.MusicScanner.getArtworkBytes(song.path)
            }
        }
    }
    val artBitmap = remember(songArtBytes) {
        songArtBytes?.let {
            try {
                android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
            } catch (e: Exception) {
                null
            }
        }
    }

    val scrollState = rememberScrollState()"""

content = content.replace(target, replacement)

target2 = """                            if (customCoverUri != null) {
                                AsyncImage(
                                    model = customCoverUri,
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { launcher.launch("image/*") }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBackground)
                                        .clickable { launcher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Pilih Gambar", tint = Color.White, modifier = Modifier.size(40.dp))
                                }
                            }"""

replacement2 = """                            if (artBitmap != null) {
                                Image(
                                    bitmap = artBitmap.asImageBitmap(),
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { launcher.launch("image/*") }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBackground)
                                        .clickable { launcher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Pilih Gambar", tint = Color.White, modifier = Modifier.size(40.dp))
                                }
                            }"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
