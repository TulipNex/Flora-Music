import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

target_vars = """    val tabConfigs by viewModel.tabConfigs.collectAsState()
    val hiddenModules by viewModel.hiddenModules.collectAsState()"""

replacement_vars = """    val tabConfigs by viewModel.tabConfigs.collectAsState()
    val hiddenModules by viewModel.hiddenModules.collectAsState()
    val customTabIcons by viewModel.customTabIcons.collectAsState()
    
    var iconPickerItem by remember { mutableStateOf<TabEditItem?>(null) }"""

content = content.replace(target_vars, replacement_vars)

target_dialog = """    if (showAddCustomDialog) {"""

replacement_dialog = """    if (iconPickerItem != null) {
        AlertDialog(
            onDismissRequest = { iconPickerItem = null },
            title = { Text("Pilih Ikon") },
            text = {
                val iconOptions = listOf(
                    "api" to Icons.Default.Whatshot,
                    "bintang" to Icons.Default.Star,
                    "bulan" to Icons.Default.DarkMode,
                    "game/console" to Icons.Default.VideogameAsset,
                    "olahraga" to Icons.Default.FitnessCenter,
                    "buku" to Icons.Default.MenuBook,
                    "gitar" to Icons.Default.MusicNote,
                    "headphone" to Icons.Default.Headphones,
                    "kamera" to Icons.Default.CameraAlt,
                    "film" to Icons.Default.Movie,
                    "mobil" to Icons.Default.DirectionsCar,
                    "pesawat" to Icons.Default.Flight,
                    "laptop" to Icons.Default.Laptop,
                    "belanja" to Icons.Default.ShoppingCart,
                    "tas" to Icons.Default.Work,
                    "folder" to Icons.Default.Folder
                )
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(iconOptions.size) { index ->
                        val (name, iconRes) = iconOptions[index]
                        IconButton(onClick = {
                            viewModel.saveCustomTabIcon(iconPickerItem!!.name, name)
                            iconPickerItem = null
                        }) {
                            Icon(iconRes, contentDescription = name, tint = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { iconPickerItem = null }) { Text("Tutup") }
            }
        )
    }

    if (showAddCustomDialog) {"""

content = content.replace(target_dialog, replacement_dialog)

target_icon = """                        val icon = when (item.name) {
                            "Beranda" -> Icons.Default.Home
                            "Lagu" -> Icons.Default.MusicNote
                            "Album" -> Icons.Default.Album
                            "Artis" -> Icons.Default.Person
                            "Favorit" -> Icons.Default.Favorite
                            "Riwayat" -> Icons.Default.History
                            "Teratas" -> Icons.Default.TrendingUp
                            "Playlist" -> Icons.Default.PlaylistPlay
                            "Genre" -> Icons.Default.Category
                            else -> Icons.Default.Folder
                        }"""

replacement_icon = """                        val customIconName = customTabIcons[item.name]
                        val icon = if (customIconName != null) {
                            when (customIconName) {
                                "api" -> Icons.Default.Whatshot
                                "bintang" -> Icons.Default.Star
                                "bulan" -> Icons.Default.DarkMode
                                "game/console" -> Icons.Default.VideogameAsset
                                "olahraga" -> Icons.Default.FitnessCenter
                                "buku" -> Icons.Default.MenuBook
                                "gitar" -> Icons.Default.MusicNote
                                "headphone" -> Icons.Default.Headphones
                                "kamera" -> Icons.Default.CameraAlt
                                "film" -> Icons.Default.Movie
                                "mobil" -> Icons.Default.DirectionsCar
                                "pesawat" -> Icons.Default.Flight
                                "laptop" -> Icons.Default.Laptop
                                "belanja" -> Icons.Default.ShoppingCart
                                "tas" -> Icons.Default.Work
                                else -> Icons.Default.Folder
                            }
                        } else {
                            when (item.name) {
                                "Beranda" -> Icons.Default.Home
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
                        }"""

content = content.replace(target_icon, replacement_icon)

target_icon_render = """                            Icon(icon, contentDescription = null, tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White)"""

replacement_icon_render = """                            if (item.isCustom) {
                                IconButton(onClick = { iconPickerItem = item }, modifier = Modifier.size(24.dp)) {
                                    Icon(icon, contentDescription = "Pilih Ikon", tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                Icon(icon, contentDescription = null, tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White)
                            }"""

content = content.replace(target_icon_render, replacement_icon_render)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
