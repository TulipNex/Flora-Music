import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

target_vars = """    private val _isPlayerCompact = MutableStateFlow(false)
    val isPlayerCompact: StateFlow<Boolean> = _isPlayerCompact.asStateFlow()"""

replacement_vars = """    private val _lyricsAlignment = MutableStateFlow("CENTER")
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
    val lyricsOtherFontColor: StateFlow<String> = _lyricsOtherFontColor.asStateFlow()"""

content = content.replace(target_vars, replacement_vars)

target_init = """        _isPlayerCompact.value = prefs.getBoolean("is_player_compact", false)"""

replacement_init = """        _lyricsAlignment.value = prefs.getString("lyrics_alignment", "CENTER") ?: "CENTER"
        _lyricsActivePosition.value = prefs.getString("lyrics_active_position", "CENTER") ?: "CENTER"
        _lyricsActiveFontSize.value = prefs.getFloat("lyrics_active_font_size", 24f)
        _lyricsOtherFontSize.value = prefs.getFloat("lyrics_other_font_size", 18f)
        _lyricsSameFontSize.value = prefs.getBoolean("lyrics_same_font_size", false)
        _lyricsUseDominantColor.value = prefs.getBoolean("lyrics_use_dominant_color", true)
        _lyricsActiveFontColor.value = prefs.getString("lyrics_active_font_color", "#10B981") ?: "#10B981"
        _lyricsOtherFontColor.value = prefs.getString("lyrics_other_font_color", "#888888") ?: "#888888\""""

content = content.replace(target_init, replacement_init)

target_funcs = """    fun setPlayerCompact(isCompact: Boolean) {
        _isPlayerCompact.value = isCompact
        getApplication<Application>()
            .getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_player_compact", isCompact)
            .apply()
    }"""

replacement_funcs = """    fun setLyricsAlignment(alignment: String) {
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
    }"""

content = content.replace(target_funcs, replacement_funcs)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
