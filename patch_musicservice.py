with open('app/src/main/java/com/example/player/MusicService.kt', 'r') as f:
    content = f.read()

import_str = "import kotlinx.coroutines.flow.firstOrNull\n"
if import_str not in content:
    content = content.replace("import kotlinx.coroutines.CoroutineScope", import_str + "import kotlinx.coroutines.CoroutineScope")

target = """                override fun onSeekTo(pos: Long) {
                    controller.seekTo(pos)
                }
            })"""

replacement = """                override fun onSeekTo(pos: Long) {
                    controller.seekTo(pos)
                }

                override fun onPlayFromSearch(query: String?, extras: android.os.Bundle?) {
                    val focus = extras?.getString(android.provider.MediaStore.EXTRA_MEDIA_FOCUS)
                    val title = extras?.getString(android.provider.MediaStore.EXTRA_MEDIA_TITLE)
                    val artist = extras?.getString(android.provider.MediaStore.EXTRA_MEDIA_ARTIST)
                    val album = extras?.getString(android.provider.MediaStore.EXTRA_MEDIA_ALBUM)
                    
                    val app = application as com.example.FloraApplication
                    val repo = app.repository
                    
                    scope.launch {
                        val songs = repo.allSongs.firstOrNull() ?: emptyList()
                        if (songs.isEmpty()) return@launch

                        var matchedSong: com.example.domain.model.Song? = null
                        var matchedQueue: List<com.example.domain.model.Song> = songs
                        val q = query ?: ""

                        when (focus) {
                            android.provider.MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE -> {
                                val pQuery = title ?: q
                                val playlists = repo.allPlaylists.firstOrNull() ?: emptyList()
                                val p = playlists.find { it.name.equals(pQuery, ignoreCase = true) }
                                if (p != null) {
                                    val pSongs = repo.getSongsForPlaylist(p.id).firstOrNull() ?: emptyList()
                                    if (pSongs.isNotEmpty()) {
                                        matchedSong = pSongs.first()
                                        matchedQueue = pSongs
                                    }
                                }
                            }
                            android.provider.MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE -> {
                                val pArtist = artist ?: q
                                val aSongs = songs.filter { it.artist.equals(pArtist, ignoreCase = true) || it.artist.contains(pArtist, ignoreCase = true) }
                                if (aSongs.isNotEmpty()) {
                                    matchedSong = aSongs.first()
                                    matchedQueue = aSongs
                                }
                            }
                            android.provider.MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE -> {
                                val pAlbum = album ?: q
                                val aSongs = songs.filter { it.album.equals(pAlbum, ignoreCase = true) || it.album.contains(pAlbum, ignoreCase = true) }
                                if (aSongs.isNotEmpty()) {
                                    matchedSong = aSongs.first()
                                    matchedQueue = aSongs
                                }
                            }
                            android.provider.MediaStore.Audio.Media.ENTRY_CONTENT_TYPE -> {
                                val pTitle = title ?: q
                                val s = songs.find { 
                                    it.title.equals(pTitle, ignoreCase = true) || 
                                    (it.title.contains(pTitle, ignoreCase = true) && (artist == null || it.artist.contains(artist, ignoreCase = true)))
                                }
                                if (s != null) {
                                    matchedSong = s
                                }
                            }
                            else -> {
                                val s = songs.find { it.title.contains(q, ignoreCase = true) || it.artist.contains(q, ignoreCase = true) || it.album.contains(q, ignoreCase = true) }
                                if (s != null) {
                                    matchedSong = s
                                }
                            }
                        }

                        if (matchedSong != null) {
                            controller.play(matchedSong, matchedQueue)
                        } else if (songs.isNotEmpty()) {
                            val looseMatch = songs.find { 
                                it.title.contains(q, ignoreCase = true) || 
                                it.artist.contains(q, ignoreCase = true) ||
                                it.album.contains(q, ignoreCase = true) 
                            }
                            if (looseMatch != null) {
                                controller.play(looseMatch, songs)
                            } else {
                                controller.play(songs.first(), songs)
                            }
                        }
                    }
                }
            })"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/player/MusicService.kt', 'w') as f:
        f.write(content)
    print("Patched MusicService.kt")
else:
    print("Target not found")
