import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """            // Perataan Lirik
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
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
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

replacement1 = """            // Perataan Lirik
            val alignmentLabels = listOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan")
            ListItem(
                headlineContent = { Text("Perataan Lirik", color = Color.White) },
                trailingContent = {
                    Row(
                        modifier = Modifier
                            .background(CardSlate, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        alignmentLabels.forEach { (value, label) ->
                            val isSelected = lyricsAlignment == value
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) EmeraldGreen else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.setLyricsAlignment(value) }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )"""

content = content.replace(target1, replacement1)

target2 = """            // Posisi Lirik Aktif
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
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
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

replacement2 = """            // Posisi Lirik Aktif
            val positionLabels = listOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah")
            ListItem(
                headlineContent = { Text("Posisi Lirik Aktif", color = Color.White) },
                trailingContent = {
                    Row(
                        modifier = Modifier
                            .background(CardSlate, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        positionLabels.forEach { (value, label) ->
                            val isSelected = lyricsActivePosition == value
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) EmeraldGreen else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.setLyricsActivePosition(value) }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
