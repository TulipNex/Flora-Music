with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

import_str = "import kotlinx.coroutines.flow.firstOrNull\n"
if "import kotlinx.coroutines.flow.firstOrNull" not in content:
    content = content.replace("import kotlinx.coroutines.flow.first\n", "import kotlinx.coroutines.flow.first\nimport kotlinx.coroutines.flow.firstOrNull\n")

target = """    // Scanning triggers
    fun scanLocalMedia(silent: Boolean = false) {"""

replacement = """    fun playFromSearch(query: String, focus: String?, title: String?, artist: String?, album: String?) {
        viewModelScope.launch {
            val songs = repository.allSongs.firstOrNull() ?: emptyList()
            if (songs.isEmpty()) return@launch

            var matchedSong: Song? = null
            var matchedQueue: List<Song> = songs

            when (focus) {
                android.provider.MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE -> {
                    val pQuery = title ?: query
                    val playlists = repository.allPlaylists.firstOrNull() ?: emptyList()
                    val p = playlists.find { it.name.equals(pQuery, ignoreCase = true) }
                    if (p != null) {
                        val pSongs = repository.getSongsForPlaylist(p.id).firstOrNull() ?: emptyList()
                        if (pSongs.isNotEmpty()) {
                            matchedSong = pSongs.first()
                            matchedQueue = pSongs
                        }
                    }
                }
                android.provider.MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE -> {
                    val pArtist = artist ?: query
                    val aSongs = songs.filter { it.artist.equals(pArtist, ignoreCase = true) || it.artist.contains(pArtist, ignoreCase = true) }
                    if (aSongs.isNotEmpty()) {
                        matchedSong = aSongs.first()
                        matchedQueue = aSongs
                    }
                }
                android.provider.MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE -> {
                    val pAlbum = album ?: query
                    val aSongs = songs.filter { it.album.equals(pAlbum, ignoreCase = true) || it.album.contains(pAlbum, ignoreCase = true) }
                    if (aSongs.isNotEmpty()) {
                        matchedSong = aSongs.first()
                        matchedQueue = aSongs
                    }
                }
                android.provider.MediaStore.Audio.Media.ENTRY_CONTENT_TYPE -> {
                    val pTitle = title ?: query
                    val s = songs.find { 
                        it.title.equals(pTitle, ignoreCase = true) || 
                        (it.title.contains(pTitle, ignoreCase = true) && (artist == null || it.artist.contains(artist, ignoreCase = true)))
                    }
                    if (s != null) {
                        matchedSong = s
                    }
                }
                else -> {
                    val s = songs.find { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) || it.album.contains(query, ignoreCase = true) }
                    if (s != null) {
                        matchedSong = s
                    }
                }
            }

            if (matchedSong != null) {
                playerController.play(matchedSong, matchedQueue)
            } else {
                val looseMatch = songs.find { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true) 
                }
                if (looseMatch != null) {
                    playerController.play(looseMatch, songs)
                } else {
                    playerController.play(songs.first(), songs)
                }
            }
        }
    }

    // Scanning triggers
    fun scanLocalMedia(silent: Boolean = false) {"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
        f.write(content)
    print("Patched MusicViewModel.kt")
else:
    print("Target not found")
