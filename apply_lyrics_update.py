with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """                    Row(
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

replacement = """                    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    
                    OutlinedTextField(
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        label = { Text("Lirik Tertanam") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen, unfocusedBorderColor = DividerSlate,
                            focusedLabelColor = EmeraldGreen, unfocusedLabelColor = TextSecondary
                        ),
                        minLines = 4, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .focusRequester(focusRequester)
                            .testTag("lyrics-editor-container")
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { focusRequester.requestFocus() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = { 
                            lyrics = lyrics.copy(selection = androidx.compose.ui.text.TextRange(0, lyrics.text.length))
                        }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Pilih Semua", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val clipText = clipboardManager.getText()?.text ?: ""
                            val text = lyrics.text
                            val selection = lyrics.selection
                            val newText = text.substring(0, selection.start) + clipText + text.substring(selection.end)
                            val newCursorIndex = selection.start + clipText.length
                            lyrics = lyrics.copy(
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(newCursorIndex)
                            )
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Tempel", tint = Color.White)
                        }
                    }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Target not found")
