import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target = """                ListItem(
                    headlineContent = { Text("Player Kompak", color = Color.White) },
                    supportingContent = { Text("Gunakan desain player yang lebih kecil", color = TextSecondary) },
                    trailingContent = {
                        Switch(
                            checked = isPlayerCompact,
                            onCheckedChange = { viewModel.setPlayerCompact(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen, checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f))
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )"""

replacement = """                ListItem(
                    modifier = Modifier.clickable { onNavigate(FloraScreen.LYRICS_SETTINGS) },
                    headlineContent = { Text("Tampilan Lirik", color = Color.White) },
                    supportingContent = { Text("Sesuaikan gaya dan posisi teks lirik", color = TextSecondary) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
