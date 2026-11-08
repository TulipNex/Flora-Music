import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

target1 = """    private val _lyricsMargin = MutableStateFlow(16f)
    val lyricsMargin: StateFlow<Float> = _lyricsMargin.asStateFlow()"""
replacement1 = """    private val _lyricsMargin = MutableStateFlow(16f)
    val lyricsMargin: StateFlow<Float> = _lyricsMargin.asStateFlow()
    
    private val _lyricsItemSpacing = MutableStateFlow(32f)
    val lyricsItemSpacing: StateFlow<Float> = _lyricsItemSpacing.asStateFlow()
    
    private val _lyricsLineHeightMultiplier = MutableStateFlow(1.5f)
    val lyricsLineHeightMultiplier: StateFlow<Float> = _lyricsLineHeightMultiplier.asStateFlow()"""

content = content.replace(target1, replacement1)

target2 = """        _lyricsMargin.value = prefs.getFloat("lyrics_margin", 16f)"""
replacement2 = """        _lyricsMargin.value = prefs.getFloat("lyrics_margin", 16f)
        _lyricsItemSpacing.value = prefs.getFloat("lyrics_item_spacing", 32f)
        _lyricsLineHeightMultiplier.value = prefs.getFloat("lyrics_line_height_multiplier", 1.5f)"""

content = content.replace(target2, replacement2)

target3 = """    fun setLyricsMargin(margin: Float) {
        _lyricsMargin.value = margin
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putFloat("lyrics_margin", margin).apply()
    }"""
replacement3 = """    fun setLyricsMargin(margin: Float) {
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
    }"""

content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
