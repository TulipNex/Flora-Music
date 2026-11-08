with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """                    // Cover Art Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (artBitmap != null) {
                                Image(
                                    bitmap = artBitmap!!.asImageBitmap(),
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { launcher.launch("image/*") }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBackground)
                                        .clickable { launcher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Pilih Gambar", tint = Color.White, modifier = Modifier.size(40.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ketuk untuk ubah sampul", fontSize = 12.sp, color = TextSecondary)
                            if (customCoverUri != null) {
                                TextButton(onClick = { customCoverUri = null }) {
                                    Text("Hapus Sampul Kustom", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }
                    }"""

replacement = """                    // Cover Art Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkBackground)
                        ) {
                            if (artBitmap != null) {
                                Image(
                                    bitmap = artBitmap!!.asImageBitmap(),
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Pilih Gambar", tint = Color.White, modifier = Modifier.size(64.dp))
                                }
                            }
                            
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (customCoverUri != null) {
                                    IconButton(
                                        onClick = { customCoverUri = null },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { launcher.launch("image/*") },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Ubah", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
        f.write(content)
    print("Updated successfully")
else:
    print("Target not found")
