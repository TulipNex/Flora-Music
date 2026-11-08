import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """fun SongMetadataDetailsDialog(
    song: Song,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {"""

# We'll use regex to match from the start of the function to the start of MetaRow
pattern = re.compile(r'fun SongMetadataDetailsDialog\(.*?fun MetaRow', re.DOTALL)

replacement = """fun SongMetadataDetailsDialog(
    song: Song,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var lyrics by remember { mutableStateOf(song.lyricsEmbedded ?: "") }
    var composer by remember { mutableStateOf(song.composer) }
    var genre by remember { mutableStateOf(song.genre) }
    var writer by remember { mutableStateOf(song.writer) }
    var trackNumber by remember { mutableStateOf(if (song.trackNumber > 0) song.trackNumber.toString() else "") }
    var discNumber by remember { mutableStateOf(song.discNumber) }
    var year by remember { mutableStateOf(if (song.year > 0) song.year.toString() else "") }
    var comment by remember { mutableStateOf(song.comment) }
    var customCoverUri by remember { mutableStateOf<Uri?>(song.customCoverUri?.let { Uri.parse(it) }) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { e.printStackTrace() }
            customCoverUri = it
        }
    }

    val scrollState = rememberScrollState()

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
                        text = "Edit Tag / Metadata",
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
                ) {
                    // Cover Art Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (customCoverUri != null) {
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
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ketuk untuk ubah sampul", fontSize = 12.sp, color = TextSecondary)
                            if (customCoverUri != null) {
                                TextButton(onClick = { customCoverUri = null }) {
                                    Text("Hapus Sampul Kustom", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        label = { Text("Artis") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = { Text("Album") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = composer,
                        onValueChange = { composer = it },
                        label = { Text("Komposer") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = writer,
                        onValueChange = { writer = it },
                        label = { Text("Penulis") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = trackNumber,
                            onValueChange = { trackNumber = it },
                            label = { Text("Nomor Trek") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                                focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                            ),
                            singleLine = true, modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discNumber,
                            onValueChange = { discNumber = it },
                            label = { Text("Nomor Kaset") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                                focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                            ),
                            singleLine = true, modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Tahun Rilis") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Komentar") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        label = { Text("Lirik Tertanam (Static/LRC)") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        minLines = 4, maxLines = 10, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Metadata Sistem (Readonly)", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                    MetaRow("Lokasi File", song.path)
                    MetaRow("Jumlah Diputar", "${song.playCount} kali")
                }

                Spacer(modifier = Modifier.height(12.dp))
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
    }
}

fun MetaRow"""

content = pattern.sub(replacement, content)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
