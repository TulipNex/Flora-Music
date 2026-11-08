import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

# find what variables are collected as state
target_state = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()"""
replacement_state = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()
    val lyricsFontFamily by viewModel.lyricsFontFamily.collectAsState()
    val lyricsCustomFontUri by viewModel.lyricsCustomFontUri.collectAsState()

    var showFontDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.setLyricsCustomFontUri(uri.toString())
            viewModel.setLyricsFontFamily("CUSTOM")
        }
    }"""

content = content.replace(target_state, replacement_state)

target_ui = """            // ==========================================
            // SECTION 2: WARNA & UKURAN FONT"""

replacement_ui = """            // ==========================================
            // SECTION 1.5: FONT & TIPOGRAFI
            // ==========================================
            SettingsSectionTitle("Font & Tipografi")
            
            val fontLabels = mapOf(
                "DEFAULT" to "Default (Sistem)",
                "SERIF" to "Serif",
                "SANS_SERIF" to "Sans Serif",
                "MONOSPACE" to "Monospace",
                "CURSIVE" to "Cursive",
                "CUSTOM" to "Custom (Impor)"
            )
            
            ListItem(
                headlineContent = { Text("Jenis Font Lirik", color = Color.White) },
                supportingContent = { Text(if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) "Custom Font" else fontLabels[lyricsFontFamily] ?: "Default", color = TextSecondary) },
                trailingContent = {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Pilih", tint = Color.White)
                },
                modifier = Modifier.clickable { showFontDialog = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            if (showFontDialog) {
                AlertDialog(
                    onDismissRequest = { showFontDialog = false },
                    title = { Text("Pilih Jenis Font", color = Color.White) },
                    text = {
                        Column {
                            fontLabels.forEach { (value, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (value == "CUSTOM") {
                                                fontPickerLauncher.launch(arrayOf("*/*"))
                                            } else {
                                                viewModel.setLyricsFontFamily(value)
                                            }
                                            showFontDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = lyricsFontFamily == value,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen, unselectedColor = Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, color = Color.White)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFontDialog = false }) {
                            Text("Batal", color = EmeraldGreen)
                        }
                    },
                    containerColor = CardSlate
                )
            }
            
            // ==========================================
            // SECTION 2: WARNA & UKURAN FONT"""

content = content.replace(target_ui, replacement_ui)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)

