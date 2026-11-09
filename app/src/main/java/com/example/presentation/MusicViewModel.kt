package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.FloraApplication
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.player.MusicPlayerController
import com.example.repository.MusicRepository
import com.example.utils.ArtworkCacheManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

import org.json.JSONArray
import org.json.JSONObject
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File

data class TabConfig(val name: String, val items: List<String>, val isCustom: Boolean = false)

@OptIn(FlowPreview::class)
class MusicViewModel(
    application: Application,
    private val repository: MusicRepository,
    val playerController: MusicPlayerController
) : AndroidViewModel(application) {

    // Library scan status
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress.asStateFlow()

    private val _scanTotal = MutableStateFlow(0)
    val scanTotal: StateFlow<Int> = _scanTotal.asStateFlow()

    // Library lists
    private val _blacklistedFolders = MutableStateFlow<Set<String>>(emptySet())
    val blacklistedFolders: StateFlow<Set<String>> = _blacklistedFolders.asStateFlow()

    private val _tabConfigs = MutableStateFlow<List<TabConfig>>(emptyList())
    val tabConfigs: StateFlow<List<TabConfig>> = _tabConfigs.asStateFlow()

    private val _customTabIcons = MutableStateFlow<Map<String, String>>(emptyMap())
    val customTabIcons: StateFlow<Map<String, String>> = _customTabIcons.asStateFlow()

    private val _hiddenModules = MutableStateFlow<List<String>>(emptyList())
    val hiddenModules: StateFlow<List<String>> = _hiddenModules.asStateFlow()

    private val _albumCoverPrefs = MutableStateFlow<Map<String, String>>(emptyMap())
    val albumCoverPrefs: StateFlow<Map<String, String>> = _albumCoverPrefs.asStateFlow()

    private val _artistCoverPrefs = MutableStateFlow<Map<String, String>>(emptyMap())
    val artistCoverPrefs: StateFlow<Map<String, String>> = _artistCoverPrefs.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _lyricsAlignment = MutableStateFlow("CENTER")
    val lyricsAlignment: StateFlow<String> = _lyricsAlignment.asStateFlow()
    
    private val _lyricsActivePosition = MutableStateFlow("CENTER")
    val lyricsActivePosition: StateFlow<String> = _lyricsActivePosition.asStateFlow()
    
    private val _lyricsActiveFontSize = MutableStateFlow(24f)
    val lyricsActiveFontSize: StateFlow<Float> = _lyricsActiveFontSize.asStateFlow()
    
    private val _lyricsOtherFontSize = MutableStateFlow(18f)
    val lyricsOtherFontSize: StateFlow<Float> = _lyricsOtherFontSize.asStateFlow()
    
    private val _lyricsSameFontSize = MutableStateFlow(false)
    val lyricsSameFontSize: StateFlow<Boolean> = _lyricsSameFontSize.asStateFlow()
    
    private val _lyricsUseDominantColor = MutableStateFlow(true)
    val lyricsUseDominantColor: StateFlow<Boolean> = _lyricsUseDominantColor.asStateFlow()
    
    private val _lyricsActiveFontColor = MutableStateFlow("#10B981")
    val lyricsActiveFontColor: StateFlow<String> = _lyricsActiveFontColor.asStateFlow()
    
    private val _lyricsOtherFontColor = MutableStateFlow("#888888")
    val lyricsOtherFontColor: StateFlow<String> = _lyricsOtherFontColor.asStateFlow()

    private val _lyricsContextLines = MutableStateFlow(3)
    val lyricsContextLines: StateFlow<Int> = _lyricsContextLines.asStateFlow()

    private val _lyricsFadeEffect = MutableStateFlow("SMOOTH")
    val lyricsFadeEffect: StateFlow<String> = _lyricsFadeEffect.asStateFlow()

    private val _lyricsMargin = MutableStateFlow(16f)
    val lyricsMargin: StateFlow<Float> = _lyricsMargin.asStateFlow()
    
    private val _lyricsItemSpacing = MutableStateFlow(32f)
    val lyricsItemSpacing: StateFlow<Float> = _lyricsItemSpacing.asStateFlow()
    
    private val _lyricsLineHeightMultiplier = MutableStateFlow(1.5f)
    val lyricsLineHeightMultiplier: StateFlow<Float> = _lyricsLineHeightMultiplier.asStateFlow()
    
    private val _lyricsFontFamily = MutableStateFlow("DEFAULT")
    val lyricsFontFamily: StateFlow<String> = _lyricsFontFamily.asStateFlow()
    
    private val _lyricsCustomFontUri = MutableStateFlow("")
    val lyricsCustomFontUri: StateFlow<String> = _lyricsCustomFontUri.asStateFlow()

    private fun getFolderPath(path: String): String {
        return try {
            java.io.File(path).parent ?: "/"
        } catch (e: Exception) {
            "/"
        }
    }

    init {
        val prefs = application.getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
        val set = prefs.getStringSet("blacklisted_folders", emptySet()) ?: emptySet()
        _blacklistedFolders.value = set

        _isGridView.value = prefs.getBoolean("is_grid_view", true)
        _lyricsAlignment.value = prefs.getString("lyrics_alignment", "CENTER") ?: "CENTER"
        _lyricsActivePosition.value = prefs.getString("lyrics_active_position", "CENTER") ?: "CENTER"
        _lyricsActiveFontSize.value = prefs.getFloat("lyrics_active_font_size", 24f)
        _lyricsOtherFontSize.value = prefs.getFloat("lyrics_other_font_size", 18f)
        _lyricsSameFontSize.value = prefs.getBoolean("lyrics_same_font_size", false)
        _lyricsUseDominantColor.value = prefs.getBoolean("lyrics_use_dominant_color", true)
        _lyricsActiveFontColor.value = prefs.getString("lyrics_active_font_color", "#10B981") ?: "#10B981"
        _lyricsOtherFontColor.value = prefs.getString("lyrics_other_font_color", "#888888") ?: "#888888"
        _lyricsContextLines.value = prefs.getInt("lyrics_context_lines", 3)
        _lyricsFadeEffect.value = prefs.getString("lyrics_fade_effect", "SMOOTH") ?: "SMOOTH"
        _lyricsMargin.value = prefs.getFloat("lyrics_margin", 16f)
        _lyricsItemSpacing.value = prefs.getFloat("lyrics_item_spacing", 32f)
        _lyricsLineHeightMultiplier.value = prefs.getFloat("lyrics_line_height_multiplier", 1.5f)
        _lyricsFontFamily.value = prefs.getString("lyrics_font_family", "DEFAULT") ?: "DEFAULT"
        _lyricsCustomFontUri.value = prefs.getString("lyrics_custom_font_uri", "") ?: ""
        
        val savedCustomIcons = prefs.getString("custom_tab_icons", null)
        if (savedCustomIcons != null) {
            try {
                val json = JSONObject(savedCustomIcons)
                val icons = mutableMapOf<String, String>()
                for (key in json.keys()) {
                    icons[key] = json.getString(key)
                }
                _customTabIcons.value = icons
            } catch (e: Exception) {}
        }

        val savedTabs = prefs.getString("tab_configs", null)
        if (savedTabs != null) {
            try {
                val json = JSONArray(savedTabs)
                val configs = mutableListOf<TabConfig>()
                val usedModules = mutableSetOf<String>()
                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)
                    val name = obj.getString("name")
                    val isCustom = obj.optBoolean("isCustom", false)
                    val itemsArr = obj.getJSONArray("items")
                    val items = mutableListOf<String>()
                    for (j in 0 until itemsArr.length()) {
                        val item = itemsArr.getString(j)
                        items.add(item)
                        usedModules.add(item)
                    }
                    configs.add(TabConfig(name, items, isCustom))
                }
                val allPossible = listOf("Lagu", "Album", "Artis", "Favorit", "Riwayat", "Teratas", "Terbaru", "Playlist", "Genre")
                val savedHidden = prefs.getString("hidden_modules", null)
                val hiddenList = mutableListOf<String>()
                if (savedHidden != null) {
                    val hiddenJson = JSONArray(savedHidden)
                    for (k in 0 until hiddenJson.length()) {
                        hiddenList.add(hiddenJson.getString(k))
                    }
                }
                for (module in allPossible) {
                    if (module !in usedModules && module !in hiddenList) {
                        if (module == "Terbaru") {
                            val berandaConfig = configs.find { it.name == "Beranda" }
                            if (berandaConfig != null && "Terbaru" !in berandaConfig.items) {
                                val newItems = berandaConfig.items.toMutableList().apply { add("Terbaru") }
                                val idx = configs.indexOf(berandaConfig)
                                configs[idx] = berandaConfig.copy(items = newItems)
                                usedModules.add("Terbaru")
                            } else {
                                hiddenList.add("Terbaru")
                            }
                        } else {
                            hiddenList.add(module)
                        }
                    }
                }
                _tabConfigs.value = configs
                _hiddenModules.value = hiddenList
            } catch (e: Exception) {
                loadDefaultTabs()
            }
        } else {
            val mainTabsStr = prefs.getString("main_tabs", "Lagu,Album,Artis")
            val oldMainTabs = mainTabsStr?.split(",")?.filter { it.isNotBlank() } ?: listOf("Lagu", "Album", "Artis")
            val subTabsStr = prefs.getString("sub_tabs", "Favorit,Riwayat,Teratas,Playlist,Genre")
            val oldSubTabs = subTabsStr?.split(",")?.filter { it.isNotBlank() } ?: listOf("Favorit", "Riwayat", "Teratas", "Playlist", "Genre")
            
            val configs = mutableListOf<TabConfig>()
            configs.add(TabConfig("Beranda", oldSubTabs.take(3), false))
            oldMainTabs.forEach { tabName ->
                if (tabName == "Koleksi") {
                    configs.add(TabConfig("Koleksi", oldSubTabs, false))
                } else {
                    configs.add(TabConfig(tabName, listOf(tabName), false))
                }
            }
            if (!oldMainTabs.contains("Koleksi") && oldSubTabs.drop(3).isNotEmpty()) {
                 configs.add(TabConfig("Koleksi", oldSubTabs.drop(3), false))
            }
            _tabConfigs.value = configs
            
            val usedModules = configs.flatMap { it.items }.toSet()
            val allPossible = listOf("Lagu", "Album", "Artis", "Favorit", "Riwayat", "Teratas", "Playlist", "Genre")
            _hiddenModules.value = allPossible.filter { it !in usedModules }
        }

        val allPrefs = prefs.all
        val albumPrefs = mutableMapOf<String, String>()
        val artistPrefs = mutableMapOf<String, String>()
        for ((key, value) in allPrefs) {
            if (key.startsWith("album_cover_") && value is String) {
                albumPrefs[key.removePrefix("album_cover_")] = value
            } else if (key.startsWith("artist_cover_") && value is String) {
                artistPrefs[key.removePrefix("artist_cover_")] = value
            }
        }
        _albumCoverPrefs.value = albumPrefs
        _artistCoverPrefs.value = artistPrefs
    }

    fun setAlbumCoverPath(albumName: String, path: String) {
        getApplication<Application>()
            .getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("album_cover_$albumName", path)
            .apply()

        _albumCoverPrefs.value = _albumCoverPrefs.value.toMutableMap().apply {
            put(albumName, path)
        }
    }

    fun setArtistCoverFromUri(artistName: String, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                // Delete existing if present
                _artistCoverPrefs.value[artistName]?.let { oldPath ->
                    val oldFile = java.io.File(oldPath)
                    if (oldFile.exists() && oldFile.absolutePath.startsWith(context.filesDir.absolutePath)) {
                        oldFile.delete()
                    }
                }
                
                val fileName = "artist_cover_${System.currentTimeMillis()}.jpg"
                val destFile = java.io.File(context.filesDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val newPath = destFile.absolutePath
                context.getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putString("artist_cover_$artistName", newPath)
                    .apply()
                
                _artistCoverPrefs.update { it.toMutableMap().apply { put(artistName, newPath) } }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeArtistCover(artistName: String) {
        val context = getApplication<Application>()
        _artistCoverPrefs.value[artistName]?.let { oldPath ->
            val oldFile = java.io.File(oldPath)
            if (oldFile.exists() && oldFile.absolutePath.startsWith(context.filesDir.absolutePath)) {
                oldFile.delete()
            }
        }
        
        context.getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .remove("artist_cover_$artistName")
            .apply()
            
        _artistCoverPrefs.update { it.toMutableMap().apply { remove(artistName) } }
    }

    fun setFolderBlacklisted(folder: String, isBlacklisted: Boolean) {
        val currentSet = _blacklistedFolders.value.toMutableSet()
        if (isBlacklisted) {
            currentSet.add(folder)
        } else {
            currentSet.remove(folder)
        }
        _blacklistedFolders.value = currentSet
        
        getApplication<Application>()
            .getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putStringSet("blacklisted_folders", currentSet)
            .apply()
    }

    fun setGridView(isGrid: Boolean) {
        _isGridView.value = isGrid
        getApplication<Application>()
            .getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_grid_view", isGrid)
            .apply()
    }

    fun setLyricsAlignment(alignment: String) {
        _lyricsAlignment.value = alignment
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_alignment", alignment).apply()
    }
    
    fun setLyricsActivePosition(position: String) {
        _lyricsActivePosition.value = position
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_active_position", position).apply()
    }
    
    fun setLyricsActiveFontSize(size: Float) {
        _lyricsActiveFontSize.value = size
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putFloat("lyrics_active_font_size", size).apply()
    }
    
    fun setLyricsOtherFontSize(size: Float) {
        _lyricsOtherFontSize.value = size
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putFloat("lyrics_other_font_size", size).apply()
    }
    
    fun setLyricsSameFontSize(same: Boolean) {
        _lyricsSameFontSize.value = same
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putBoolean("lyrics_same_font_size", same).apply()
    }
    
    fun setLyricsUseDominantColor(use: Boolean) {
        _lyricsUseDominantColor.value = use
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putBoolean("lyrics_use_dominant_color", use).apply()
    }
    
    fun setLyricsActiveFontColor(colorHex: String) {
        _lyricsActiveFontColor.value = colorHex
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_active_font_color", colorHex).apply()
    }
    
    fun setLyricsOtherFontColor(colorHex: String) {
        _lyricsOtherFontColor.value = colorHex
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_other_font_color", colorHex).apply()
    }

    fun setLyricsContextLines(lines: Int) {
        _lyricsContextLines.value = lines
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putInt("lyrics_context_lines", lines).apply()
    }

    fun setLyricsFadeEffect(effect: String) {
        _lyricsFadeEffect.value = effect
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_fade_effect", effect).apply()
    }

    fun setLyricsMargin(margin: Float) {
        _lyricsMargin.value = margin
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putFloat("lyrics_margin", margin).apply()
    }
    
    fun setLyricsItemSpacing(spacing: Float) {
        _lyricsItemSpacing.value = spacing
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putFloat("lyrics_item_spacing", spacing).apply()
    }
    
    fun setLyricsLineHeightMultiplier(multiplier: Float) {
        _lyricsLineHeightMultiplier.value = multiplier
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putFloat("lyrics_line_height_multiplier", multiplier).apply()
    }
    
    fun setLyricsFontFamily(family: String) {
        _lyricsFontFamily.value = family
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_font_family", family).apply()
    }
    
    fun setLyricsCustomFontUri(uri: String) {
        _lyricsCustomFontUri.value = uri
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_custom_font_uri", uri).apply()
    }

    
    fun resetTabsToDefault() {
        val defaultConfigs = listOf(
            TabConfig("Beranda", listOf("Favorit", "Riwayat", "Teratas", "Terbaru")),
            TabConfig("Lagu", listOf("Lagu")),
            TabConfig("Album", listOf("Album")),
            TabConfig("Artis", listOf("Artis")),
            TabConfig("Koleksi", listOf("Playlist", "Genre"))
        )
        saveTabConfigs(defaultConfigs, emptyList())
    }

    private fun loadDefaultTabs() {
        _tabConfigs.value = listOf(
            TabConfig("Beranda", listOf("Favorit", "Riwayat", "Teratas", "Terbaru")),
            TabConfig("Lagu", listOf("Lagu")),
            TabConfig("Album", listOf("Album")),
            TabConfig("Artis", listOf("Artis")),
            TabConfig("Koleksi", listOf("Playlist", "Genre"))
        )
        _hiddenModules.value = emptyList()
    }

    fun saveTabConfigs(configs: List<TabConfig>, hidden: List<String>): Boolean {
        val invalidSubtabs = listOf("Lagu", "Artis", "Album")
        val hasInvalidSubtab = configs.any { config ->
            val isSubtab = config.items.any { item ->
                item in invalidSubtabs && (config.isCustom || item != config.name)
            }
            val hasSubtabs = config.name in invalidSubtabs && config.items.size > 1
            isSubtab || hasSubtabs
        }
        
        if (hasInvalidSubtab) {
            return false
        }

        _tabConfigs.value = configs
        _hiddenModules.value = hidden
        
        val json = JSONArray()
        for (c in configs) {
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("isCustom", c.isCustom)
            val itemsArr = JSONArray()
            c.items.forEach { itemsArr.put(it) }
            obj.put("items", itemsArr)
            json.put(obj)
        }
        
        val hiddenJson = JSONArray()
        hidden.forEach { hiddenJson.put(it) }
        
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("tab_configs", json.toString())
            .putString("hidden_modules", hiddenJson.toString())
            .apply()
        return true
    }

    val allAvailableFolders: StateFlow<Set<String>> = repository.allSongs
        .map { songs ->
            songs.map { getFolderPath(it.path) }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allSongs: StateFlow<List<Song>> = combine(
        repository.allSongs,
        _blacklistedFolders
    ) { songs, blacklist ->
        songs.filter { getFolderPath(it.path) !in blacklist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = combine(
        repository.favoriteSongs,
        _blacklistedFolders
    ) { songs, blacklist ->
        songs.filter { getFolderPath(it.path) !in blacklist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = combine(
        repository.recentlyPlayedSongs,
        _blacklistedFolders
    ) { songs, blacklist ->
        songs.filter { getFolderPath(it.path) !in blacklist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = combine(
        repository.mostPlayedSongs,
        _blacklistedFolders
    ) { songs, blacklist ->
        songs.filter { getFolderPath(it.path) !in blacklist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<Song>> = allSongs.map { songs ->
        val oneWeekAgoSeconds = (System.currentTimeMillis() / 1000) - (7 * 24 * 60 * 60)
        songs.filter {
            val songSeconds = if (it.dateAdded > 10_000_000_000L) it.dateAdded / 1000 else it.dateAdded
            songSeconds >= oneWeekAgoSeconds
        }.sortedByDescending { it.dateAdded }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query and realtime results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Song>> = combine(
        _searchQuery.debounce(300).flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchSongs(query)
            }
        },
        _blacklistedFolders
    ) { songs, blacklist ->
        songs.filter { getFolderPath(it.path) !in blacklist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Active Selections
    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    val songsInSelectedPlaylist: StateFlow<List<Song>> = combine(
        _selectedPlaylistId.flatMapLatest { playlistId ->
            if (playlistId == null) {
                flowOf(emptyList())
            } else {
                repository.getSongsInPlaylist(playlistId)
            }
        },
        _blacklistedFolders
    ) { songs, blacklist ->
        songs.filter { getFolderPath(it.path) !in blacklist }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playFromSearch(query: String, focus: String?, title: String?, artist: String?, album: String?) {
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
                        val pSongs = repository.getSongsInPlaylist(p.id).firstOrNull() ?: emptyList()
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
    fun scanLocalMedia(silent: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!silent) {
                _isScanning.value = true
            }
            _scanProgress.value = 0
            _scanTotal.value = 0
            repository.scanLocalLibrary(getApplication()) { current, total ->
                _scanProgress.value = current
                _scanTotal.value = total
            }
            // Trigger pre-extraction of album artwork thumbnails
            com.example.service.ArtworkPreExtractorService.start(getApplication())
            if (!silent) {
                _isScanning.value = false
            }
        }
    }

    fun clearLibrary() {
        viewModelScope.launch {
            repository.clearLibrary()
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            if (playerController.currentSong.value?.id == song.id) {
                playerController.skipToNext()
            }
            repository.deleteSong(song, getApplication())
        }
    }

    // Favorite toggle
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val newFavState = !song.isFavorite
            repository.setSongFavorite(song.id, newFavState)
            playerController.updateCurrentSongReference(song.copy(isFavorite = newFavState))
        }
    }

    // Playback control wrappers
    fun playSong(song: Song, queue: List<Song>) {
        playerController.play(song, queue)
    }

    // Playlist operations
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun selectPlaylist(playlistId: Long?) {
        _selectedPlaylistId.value = playlistId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSongMetadata(
        songId: Long, 
        newTitle: String, 
        newArtist: String, 
        newAlbum: String, 
        newLyrics: String,
        newComposer: String,
        newGenre: String,
        newWriter: String,
        newTrackNumber: Int,
        newDiscNumber: String,
        newYear: Int,
        newComment: String,
        newCustomCoverUri: String?
    ) {
        viewModelScope.launch {
            repository.getSongById(songId)?.let { currentSong ->
                val updated = currentSong.copy(
                    title = newTitle,
                    artist = newArtist,
                    album = newAlbum,
                    lyricsEmbedded = newLyrics.takeIf { it.isNotBlank() },
                    composer = newComposer,
                    genre = newGenre,
                    writer = newWriter,
                    trackNumber = newTrackNumber,
                    discNumber = newDiscNumber,
                    year = newYear,
                    comment = newComment,
                    customCoverUri = newCustomCoverUri,
                    dateModified = System.currentTimeMillis() / 1000 // Standard Unix timestamp
                )

                // Try to write to the physical file using Jaudiotagger
                try {
                    val f = File(updated.path)
                    if (f.exists()) {
                        val audioFile = AudioFileIO.read(f)
                        val tag = audioFile.tagOrCreateAndSetDefault
                        
                        tag.setField(FieldKey.TITLE, updated.title)
                        tag.setField(FieldKey.ARTIST, updated.artist)
                        tag.setField(FieldKey.ALBUM, updated.album)
                        tag.setField(FieldKey.LYRICS, updated.lyricsEmbedded ?: "")
                        tag.setField(FieldKey.COMPOSER, updated.composer)
                        tag.setField(FieldKey.GENRE, updated.genre)
                        
                        if (updated.trackNumber > 0) tag.setField(FieldKey.TRACK, updated.trackNumber.toString()) else tag.deleteField(FieldKey.TRACK)
                        if (updated.discNumber.isNotBlank()) tag.setField(FieldKey.DISC_NO, updated.discNumber) else tag.deleteField(FieldKey.DISC_NO)
                        if (updated.year > 0) tag.setField(FieldKey.YEAR, updated.year.toString()) else tag.deleteField(FieldKey.YEAR)
                        if (updated.comment.isNotBlank()) tag.setField(FieldKey.COMMENT, updated.comment) else tag.deleteField(FieldKey.COMMENT)
                        
                        if (updated.customCoverUri != null) {
                            try {
                                val uri = android.net.Uri.parse(updated.customCoverUri)
                                getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                                    val bytes = inputStream.readBytes()
                                    val artwork = ArtworkFactory.getNew()
                                    artwork.binaryData = bytes
                                    artwork.mimeType = "image/jpeg"
                                    tag.deleteArtworkField()
                                    tag.setField(artwork)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        
                        audioFile.commit()
                        // Force refresh artwork cache
                        ArtworkCacheManager.cacheArtwork(getApplication(), updated.path, force = true)
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(getApplication(), "Metadata file berhasil diperbarui", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(getApplication(), "Gagal mengubah file fisik: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                repository.updateSong(updated)
                playerController.updateCurrentSongReference(updated)
            }
        }
    }

    fun saveCustomTabIcon(tabName: String, iconName: String) {
        val current = _customTabIcons.value.toMutableMap()
        current[tabName] = iconName
        _customTabIcons.value = current
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("custom_tab_icons", JSONObject(current.toMap()).toString()).apply()
        }
    }

    // ViewModel Factory helper
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = FloraApplication.instance
                return MusicViewModel(app, app.repository, app.playerController) as T
            }
        }
    }
}
