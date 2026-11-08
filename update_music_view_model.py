import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

# Add to StateFlows
props_target = """    private val _lyricsOtherFontColor = MutableStateFlow("#888888")
    val lyricsOtherFontColor: StateFlow<String> = _lyricsOtherFontColor.asStateFlow()"""

props_replacement = """    private val _lyricsOtherFontColor = MutableStateFlow("#888888")
    val lyricsOtherFontColor: StateFlow<String> = _lyricsOtherFontColor.asStateFlow()

    private val _lyricsContextLines = MutableStateFlow(3)
    val lyricsContextLines: StateFlow<Int> = _lyricsContextLines.asStateFlow()

    private val _lyricsFadeEffect = MutableStateFlow("SMOOTH")
    val lyricsFadeEffect: StateFlow<String> = _lyricsFadeEffect.asStateFlow()"""
content = content.replace(props_target, props_replacement)

# Add to init
init_target = """        _lyricsActiveFontColor.value = prefs.getString("lyrics_active_font_color", "#10B981") ?: "#10B981"
        _lyricsOtherFontColor.value = prefs.getString("lyrics_other_font_color", "#888888") ?: "#888888\""""

init_replacement = """        _lyricsActiveFontColor.value = prefs.getString("lyrics_active_font_color", "#10B981") ?: "#10B981"
        _lyricsOtherFontColor.value = prefs.getString("lyrics_other_font_color", "#888888") ?: "#888888"
        _lyricsContextLines.value = prefs.getInt("lyrics_context_lines", 3)
        _lyricsFadeEffect.value = prefs.getString("lyrics_fade_effect", "SMOOTH") ?: "SMOOTH\""""
content = content.replace(init_target, init_replacement)

# Add setters
setters_target = """    fun setLyricsOtherFontColor(colorHex: String) {
        _lyricsOtherFontColor.value = colorHex
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_other_font_color", colorHex).apply()
    }"""

setters_replacement = """    fun setLyricsOtherFontColor(colorHex: String) {
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
    }"""
content = content.replace(setters_target, setters_replacement)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
