import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """            // Perataan Lirik
            val alignmentLabels = listOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan")
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
                                    text = alignmentLabels.find { it.first == lyricsAlignment }?.second ?: "Tengah",
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
                            modifier = Modifier
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                                alignmentLabels.forEach { (value, label) ->
                                    val isSelected = lyricsAlignment == value
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) EmeraldGreen else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { 
                                                viewModel.setLyricsAlignment(value)
                                                showAlignmentDialog = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
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
                    Box {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showAlignmentDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = alignmentLabels.find { it.first == lyricsAlignment }?.second ?: "Tengah",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
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
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                alignmentLabels.forEach { (value, label) ->
                                    val isSelected = lyricsAlignment == value
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { 
                                                viewModel.setLyricsAlignment(value)
                                                showAlignmentDialog = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )"""

content = content.replace(target1, replacement1)

target2 = """            // Posisi Lirik Aktif
            val positionLabels = listOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah")
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
                                    text = positionLabels.find { it.first == lyricsActivePosition }?.second ?: "Tengah",
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
                            modifier = Modifier
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                                positionLabels.forEach { (value, label) ->
                                    val isSelected = lyricsActivePosition == value
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) EmeraldGreen else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { 
                                                viewModel.setLyricsActivePosition(value)
                                                showPositionDialog = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
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
                    Box {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showPositionDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = positionLabels.find { it.first == lyricsActivePosition }?.second ?: "Tengah",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
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
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                positionLabels.forEach { (value, label) ->
                                    val isSelected = lyricsActivePosition == value
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { 
                                                viewModel.setLyricsActivePosition(value)
                                                showPositionDialog = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
