import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

# Restore the spacers I accidentally broke
content = content.replace("Spacer(modifier = Modifier.width(1.dp))", "Spacer(modifier = Modifier.width(8.dp))")

# Fix the dropdowns
target_perataan = """            // Perataan Lirik
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
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }"""

replacement_perataan = """            // Perataan Lirik
            val alignmentLabels = mapOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan")
            ListItem(
                headlineContent = { Text("Perataan Lirik", color = Color.White) },
                trailingContent = {
                    Box {
                        Box(
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

content = content.replace(target_perataan, replacement_perataan)

target_posisi = """            // Posisi Lirik Aktif
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
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }"""

replacement_posisi = """            // Posisi Lirik Aktif
            val positionLabels = mapOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah")
            ListItem(
                headlineContent = { Text("Posisi Lirik Aktif", color = Color.White) },
                trailingContent = {
                    Box {
                        Box(
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

content = content.replace(target_posisi, replacement_posisi)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
