import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target_vars = """    val isGridView by viewModel.isGridView.collectAsState()

    val tabConfigs by viewModel.tabConfigs.collectAsState()"""

replacement_vars = """    val isGridView by viewModel.isGridView.collectAsState()

    val tabConfigs by viewModel.tabConfigs.collectAsState()
    val customTabIcons by viewModel.customTabIcons.collectAsState()"""

content = content.replace(target_vars, replacement_vars)

target_icon = """                    val icon = when (tab) {
                        "Beranda" -> Icons.Default.Home
                        "Lagu" -> Icons.Default.MusicNote
                        "Album" -> Icons.Default.Album
                        "Artis" -> Icons.Default.Person
                        "Favorit" -> Icons.Default.Favorite
                        "Riwayat" -> Icons.Default.History
                        "Teratas" -> Icons.Default.TrendingUp
                        "Playlist" -> Icons.Default.PlaylistPlay
                        "Genre" -> Icons.Default.Category
                        "Koleksi" -> Icons.Default.LibraryMusic
                        else -> Icons.Default.MusicNote
                    }"""

replacement_icon = """                    val customIconName = customTabIcons[tab]
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
                        when (tab) {
                            "Beranda" -> Icons.Default.Home
                            "Lagu" -> Icons.Default.MusicNote
                            "Album" -> Icons.Default.Album
                            "Artis" -> Icons.Default.Person
                            "Favorit" -> Icons.Default.Favorite
                            "Riwayat" -> Icons.Default.History
                            "Teratas" -> Icons.Default.TrendingUp
                            "Playlist" -> Icons.Default.PlaylistPlay
                            "Genre" -> Icons.Default.Category
                            "Koleksi" -> Icons.Default.LibraryMusic
                            else -> Icons.Default.MusicNote
                        }
                    }"""

content = content.replace(target_icon, replacement_icon)

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
