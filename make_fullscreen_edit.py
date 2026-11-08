import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Replace the Dialog and Card wrapper
target_wrapper_start = """    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Metadata",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable fields form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                ) {"""

replacement_wrapper_start = """    val scrollState = rememberScrollState()

    androidx.activity.compose.BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .zIndex(100f)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Row (TopAppBar style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceSlate)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Text(
                    text = "Edit Metadata",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                TextButton(
                    onClick = {
                        viewModel.updateSongMetadata(
                            songId = song.id,
                            newTitle = title,
                            newArtist = artist,
                            newAlbum = album,
                            newLyrics = lyrics,
                            newComposer = composer,
                            newGenre = genre,
                            newWriter = writer,
                            newTrackNumber = trackNumber.toIntOrNull() ?: 0,
                            newDiscNumber = discNumber,
                            newYear = year.toIntOrNull() ?: 0,
                            newComment = comment,
                            newCustomCoverUri = customCoverUri?.toString()
                        )
                        onDismiss()
                    }
                ) {
                    Text("SIMPAN", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            }

            // Scrollable fields form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {"""

# Replace the wrapper end
target_wrapper_end = """                Spacer(modifier = Modifier.height(12.dp))
                // Action Buttons at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Batal", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            viewModel.updateSongMetadata(
                                songId = song.id,
                                newTitle = title,
                                newArtist = artist,
                                newAlbum = album,
                                newLyrics = lyrics,
                                newComposer = composer,
                                newGenre = genre,
                                newWriter = writer,
                                newTrackNumber = trackNumber.toIntOrNull() ?: 0,
                                newDiscNumber = discNumber,
                                newYear = year.toIntOrNull() ?: 0,
                                newComment = comment,
                                newCustomCoverUri = customCoverUri?.toString()
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("Simpan", color = SoftWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }"""

replacement_wrapper_end = """            }
        }
    }"""

content = content.replace(target_wrapper_start, replacement_wrapper_start)
content = content.replace(target_wrapper_end, replacement_wrapper_end)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
