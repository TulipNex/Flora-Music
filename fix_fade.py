import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """            // Efek Memudar (Fade Effect)
            Text(
                text = "Efek Memudar",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SMOOTH" to "Halus", "SHARP" to "Tajam", "NONE" to "Mati").forEach { (value, label) ->
                    val isSelected = lyricsFadeEffect == value
                    Button(
                        onClick = { viewModel.setLyricsFadeEffect(value) },
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

replacement = """            // Efek Memudar (Fade Effect)
            val fadeLabels = listOf("SMOOTH" to "Halus", "SHARP" to "Tajam", "NONE" to "Mati")
            ListItem(
                headlineContent = { Text("Efek Memudar", color = Color.White) },
                trailingContent = {
                    Box {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showFadeDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fadeLabels.find { it.first == lyricsFadeEffect }?.second ?: "Halus",
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
                        
                        if (showFadeDialog) {
                            androidx.compose.ui.window.Popup(
                                alignment = Alignment.TopStart,
                                onDismissRequest = { showFadeDialog = false },
                                properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .background(CardSlate, RoundedCornerShape(8.dp))
                                        .padding(3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        fadeLabels.forEach { (value, label) ->
                                            val isSelected = lyricsFadeEffect == value
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable { 
                                                        viewModel.setLyricsFadeEffect(value)
                                                        showFadeDialog = false
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
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
