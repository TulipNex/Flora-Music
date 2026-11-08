import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "import androidx.compose.material.icons.filled.KeyboardArrowRight",
    "import androidx.compose.material.icons.filled.KeyboardArrowRight\nimport androidx.compose.material.icons.filled.KeyboardArrowDown"
)

target_perataan = """            // Perataan Lirik
            val alignmentLabels = mapOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan")
            ListItem(
                headlineContent = { Text("Perataan Lirik", color = Color.White) },
                supportingContent = { Text(alignmentLabels[lyricsAlignment] ?: "Tengah", color = TextSecondary) },
                trailingContent = {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Pilih", tint = Color.White)
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
            }"""

replacement_perataan = """            // Perataan Lirik
            val alignmentLabels = mapOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan")
            ListItem(
                headlineContent = { Text("Perataan Lirik", color = Color.White) },
                trailingContent = {
                    Box {
                        Box(
                            modifier = Modifier
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showAlignmentDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = alignmentLabels[lyricsAlignment] ?: "Tengah",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showAlignmentDialog,
                            onDismissRequest = { showAlignmentDialog = false },
                            modifier = Modifier.background(CardSlate)
                        ) {
                            alignmentLabels.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = Color.White) },
                                    onClick = {
                                        viewModel.setLyricsAlignment(value)
                                        showAlignmentDialog = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )"""

content = content.replace(target_perataan, replacement_perataan)

target_posisi = """            // Posisi Lirik Aktif
            val positionLabels = mapOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah")
            ListItem(
                headlineContent = { Text("Posisi Lirik Aktif", color = Color.White) },
                supportingContent = { Text(positionLabels[lyricsActivePosition] ?: "Tengah", color = TextSecondary) },
                trailingContent = {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Pilih", tint = Color.White)
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

replacement_posisi = """            // Posisi Lirik Aktif
            val positionLabels = mapOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah")
            ListItem(
                headlineContent = { Text("Posisi Lirik Aktif", color = Color.White) },
                trailingContent = {
                    Box {
                        Box(
                            modifier = Modifier
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showPositionDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = positionLabels[lyricsActivePosition] ?: "Tengah",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showPositionDialog,
                            onDismissRequest = { showPositionDialog = false },
                            modifier = Modifier.background(CardSlate)
                        ) {
                            positionLabels.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = Color.White) },
                                    onClick = {
                                        viewModel.setLyricsActivePosition(value)
                                        showPositionDialog = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )"""

content = content.replace(target_posisi, replacement_posisi)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
