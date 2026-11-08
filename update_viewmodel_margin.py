import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

# Add to StateFlows
props_target = """    private val _lyricsFadeEffect = MutableStateFlow("SMOOTH")
    val lyricsFadeEffect: StateFlow<String> = _lyricsFadeEffect.asStateFlow()"""

props_replacement = """    private val _lyricsFadeEffect = MutableStateFlow("SMOOTH")
    val lyricsFadeEffect: StateFlow<String> = _lyricsFadeEffect.asStateFlow()

    private val _lyricsMargin = MutableStateFlow(16f)
    val lyricsMargin: StateFlow<Float> = _lyricsMargin.asStateFlow()"""
content = content.replace(props_target, props_replacement)

# Add to init
init_target = """        _lyricsFadeEffect.value = prefs.getString("lyrics_fade_effect", "SMOOTH") ?: "SMOOTH\""""

init_replacement = """        _lyricsFadeEffect.value = prefs.getString("lyrics_fade_effect", "SMOOTH") ?: "SMOOTH"
        _lyricsMargin.value = prefs.getFloat("lyrics_margin", 16f)"""
content = content.replace(init_target, init_replacement)

# Add setter
setters_target = """    fun setLyricsFadeEffect(effect: String) {
        _lyricsFadeEffect.value = effect
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_fade_effect", effect).apply()
    }"""

setters_replacement = """    fun setLyricsFadeEffect(effect: String) {
        _lyricsFadeEffect.value = effect
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putString("lyrics_fade_effect", effect).apply()
    }

    fun setLyricsMargin(margin: Float) {
        _lyricsMargin.value = margin
        getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE).edit().putFloat("lyrics_margin", margin).apply()
    }"""
content = content.replace(setters_target, setters_replacement)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
