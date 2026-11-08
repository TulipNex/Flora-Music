with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target1 = """    var lyrics by remember { mutableStateOf(song.lyricsEmbedded ?: "") }"""
replacement1 = """    var lyrics by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(song.lyricsEmbedded ?: "")) }
    val playbackPosition by viewModel.playerController.playbackPosition.collectAsState()"""

if target1 in content:
    content = content.replace(target1, replacement1)

target2 = """                                newLyrics = lyrics,"""
replacement2 = """                                newLyrics = lyrics.text,"""
if target2 in content:
    content = content.replace(target2, replacement2)

target3 = """                    OutlinedTextField(
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        label = { Text("Lirik Tertanam") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        minLines = 4, maxLines = 10, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )"""
replacement3 = """                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lirik Tertanam", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = {
                                val positionMs = playbackPosition
                                val minutes = (positionMs / 1000) / 60
                                val seconds = (positionMs / 1000) % 60
                                val centis = (positionMs % 1000) / 10
                                val timeString = String.format("[%02d:%02d.%02d]", minutes, seconds, centis)
                                
                                val text = lyrics.text
                                val selection = lyrics.selection
                                val newText = text.substring(0, selection.start) + timeString + text.substring(selection.end)
                                val newCursorIndex = selection.start + timeString.length
                                
                                lyrics = lyrics.copy(
                                    text = newText,
                                    selection = androidx.compose.ui.text.TextRange(newCursorIndex)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp).testTag("insert_timestamp_btn")
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sisipkan Waktu", fontSize = 12.sp, color = EmeraldGreen)
                        }
                    }
                    OutlinedTextField(
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        minLines = 4, maxLines = 10, modifier = Modifier.fillMaxWidth().testTag("lyrics-editor-container")
                    )"""

if target3 in content:
    content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

