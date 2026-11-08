with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

target = """                        val icon = when (item.name) {
                            "Lagu" -> Icons.Default.MusicNote
                            "Album" -> Icons.Default.Album
                            "Artis" -> Icons.Default.Person
                            "Favorit" -> Icons.Default.Favorite
                            "Riwayat" -> Icons.Default.History
                            "Teratas" -> Icons.Default.TrendingUp
                            "Playlist" -> Icons.Default.PlaylistPlay
                            "Genre" -> Icons.Default.Category
                            else -> Icons.Default.Folder
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .background(if (isDragging) CardSlate else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DragHandle, contentDescription = "Geser", tint = TextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            if (item.role == TabRole.SUB) {
                                Spacer(modifier = Modifier.width(24.dp))
                                Icon(Icons.Default.SubdirectoryArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            Icon(icon, contentDescription = null, tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.name, 
                                color = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White, 
                                fontSize = 16.sp, 
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (item.isCustom) {
                                IconButton(onClick = {
                                    allItems.remove(item)
                                    validateAndSaveFn()
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val nextRole = when (item.role) {
                                            TabRole.MAIN -> TabRole.SUB
                                            else -> TabRole.MAIN
                                        }
                                        val idx = allItems.indexOf(item)
                                        if (idx != -1) {
                                            allItems[idx] = item.copy(role = nextRole)
                                            validateAndSaveFn()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(item.role.label, fontSize = 12.sp, color = if (item.role == TabRole.HIDDEN) TextSecondary else EmeraldGreen)
                                }
                            }
                        }"""

replacement = """                    if (item.role == TabRole.HEADER) {
                        Text(
                            text = item.name,
                            color = EmeraldGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        )
                    } else {
                        val icon = when (item.name) {
                            "Lagu" -> Icons.Default.MusicNote
                            "Album" -> Icons.Default.Album
                            "Artis" -> Icons.Default.Person
                            "Favorit" -> Icons.Default.Favorite
                            "Riwayat" -> Icons.Default.History
                            "Teratas" -> Icons.Default.TrendingUp
                            "Playlist" -> Icons.Default.PlaylistPlay
                            "Genre" -> Icons.Default.Category
                            else -> Icons.Default.Folder
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .background(if (isDragging) CardSlate else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DragHandle, contentDescription = "Geser", tint = TextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            if (item.role == TabRole.SUB) {
                                Spacer(modifier = Modifier.width(24.dp))
                                Icon(Icons.Default.SubdirectoryArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            Icon(icon, contentDescription = null, tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.name, 
                                color = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White, 
                                fontSize = 16.sp, 
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (item.isCustom) {
                                IconButton(onClick = {
                                    allItems.remove(item)
                                    validateAndSaveFn()
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            } else if (item.role != TabRole.HIDDEN) {
                                Button(
                                    onClick = {
                                        val nextRole = when (item.role) {
                                            TabRole.MAIN -> TabRole.SUB
                                            else -> TabRole.MAIN
                                        }
                                        val idx = allItems.indexOf(item)
                                        if (idx != -1) {
                                            allItems[idx] = item.copy(role = nextRole)
                                            validateAndSaveFn()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(item.role.label, fontSize = 12.sp, color = EmeraldGreen)
                                }
                            }
                        }
                    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
