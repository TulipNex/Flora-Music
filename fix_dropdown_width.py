import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showAlignmentDialog = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = alignmentLabels[lyricsAlignment] ?: "Tengah",
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
                        }"""

replacement1 = """                        Box(
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
                        }"""

content = content.replace(target1, replacement1)

target2 = """                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showPositionDialog = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = positionLabels[lyricsActivePosition] ?: "Tengah",
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
                        }"""

replacement2 = """                        Box(
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
                        }"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
