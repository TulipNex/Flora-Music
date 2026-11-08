import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

decl_target = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()"""
decl_replacement = """    val lyricsMargin by viewModel.lyricsMargin.collectAsState()
    
    var showAlignmentDialog by remember { mutableStateOf(false) }
    var showPositionDialog by remember { mutableStateOf(false) }"""
content = content.replace(decl_target, decl_replacement)

# Import necessary icons if they are not there
# Icons.AutoMirrored.Filled.KeyboardArrowRight
# We'll use Icons.Default.KeyboardArrowRight

ui_target = """            // Perataan
            Text(
                text = "Perataan Lirik",
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan").forEach { (value, label) ->
                    val isSelected = lyricsAlignment == value
                    Button(
                        onClick = { viewModel.setLyricsAlignment(value) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) EmeraldGreen else CardSlate,
                            contentColor = if (isSelected) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }

            HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))

            // Posisi Lirik Aktif
            Text(
                text = "Posisi Lirik Aktif",
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah").forEach { (value, label) ->
                    val isSelected = lyricsActivePosition == value
                    Button(
                        onClick = { viewModel.setLyricsActivePosition(value) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) EmeraldGreen else CardSlate,
                            contentColor = if (isSelected) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }"""

ui_replacement = """            // Perataan
            val alignmentLabels = mapOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan")
            ListItem(
                headlineContent = { Text("Perataan Lirik", color = Color.White) },
                supportingContent = { Text(alignmentLabels[lyricsAlignment] ?: "Tengah", color = TextSecondary) },
                trailingContent = {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Pilih", tint = Color.White)
                },
                modifier = Modifier.clickable { showAlignmentDialog = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            if (showAlignmentDialog) {
                AlertDialog(
                    onDismissRequest = { showAlignmentDialog = false },
                    title = { Text("Perataan Lirik", color = Color.White) },
                    text = {
                        Column {
                            alignmentLabels.forEach { (value, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setLyricsAlignment(value)
                                            showAlignmentDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = lyricsAlignment == value,
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
                        TextButton(onClick = { showAlignmentDialog = false }) {
                            Text("Batal", color = EmeraldGreen)
                        }
                    },
                    containerColor = CardSlate
                )
            }

            // Posisi Lirik Aktif
            val positionLabels = mapOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah")
            ListItem(
                headlineContent = { Text("Posisi Lirik Aktif", color = Color.White) },
                supportingContent = { Text(positionLabels[lyricsActivePosition] ?: "Tengah", color = TextSecondary) },
                trailingContent = {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Pilih", tint = Color.White)
                },
                modifier = Modifier.clickable { showPositionDialog = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            if (showPositionDialog) {
                AlertDialog(
                    onDismissRequest = { showPositionDialog = false },
                    title = { Text("Posisi Lirik Aktif", color = Color.White) },
                    text = {
                        Column {
                            positionLabels.forEach { (value, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setLyricsActivePosition(value)
                                            showPositionDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = lyricsActivePosition == value,
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
                        TextButton(onClick = { showPositionDialog = false }) {
                            Text("Batal", color = EmeraldGreen)
                        }
                    },
                    containerColor = CardSlate
                )
            }"""

content = content.replace(ui_target, ui_replacement)

# check if we need to add KeyboardArrowRight import
# Just checking in general

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)

