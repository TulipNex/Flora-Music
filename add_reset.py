with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

reset_fn = """
    fun resetTabsToDefault() {
        val defaultConfigs = listOf(
            TabConfig("Beranda", listOf("Favorit", "Riwayat", "Teratas")),
            TabConfig("Lagu", listOf("Lagu")),
            TabConfig("Album", listOf("Album")),
            TabConfig("Artis", listOf("Artis")),
            TabConfig("Koleksi", listOf("Playlist", "Genre"))
        )
        saveTabConfigs(defaultConfigs, emptyList())
    }
"""

content = content.replace("private fun loadDefaultTabs() {", reset_fn + "\n    private fun loadDefaultTabs() {")

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
