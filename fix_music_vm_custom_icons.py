import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

target_vars = """    private val _tabConfigs = MutableStateFlow<List<TabConfig>>(emptyList())
    val tabConfigs: StateFlow<List<TabConfig>> = _tabConfigs.asStateFlow()"""

replacement_vars = """    private val _tabConfigs = MutableStateFlow<List<TabConfig>>(emptyList())
    val tabConfigs: StateFlow<List<TabConfig>> = _tabConfigs.asStateFlow()

    private val _customTabIcons = MutableStateFlow<Map<String, String>>(emptyMap())
    val customTabIcons: StateFlow<Map<String, String>> = _customTabIcons.asStateFlow()"""

content = content.replace(target_vars, replacement_vars)

target_init = """        _isGridView.value = prefs.getBoolean("is_grid_view", true)
        _isPlayerCompact.value = prefs.getBoolean("is_player_compact", false)"""

replacement_init = """        _isGridView.value = prefs.getBoolean("is_grid_view", true)
        _isPlayerCompact.value = prefs.getBoolean("is_player_compact", false)
        
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
        }"""

content = content.replace(target_init, replacement_init)

target_funcs = """    fun setPlayerCompact(compact: Boolean) {
        _isPlayerCompact.value = compact
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_player_compact", compact).apply()
        }
    }"""

replacement_funcs = """    fun setPlayerCompact(compact: Boolean) {
        _isPlayerCompact.value = compact
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_player_compact", compact).apply()
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
    
    fun removeCustomTabIcon(tabName: String) {
        val current = _customTabIcons.value.toMutableMap()
        current.remove(tabName)
        _customTabIcons.value = current
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>().getSharedPreferences("flora_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("custom_tab_icons", JSONObject(current.toMap()).toString()).apply()
        }
    }"""

content = content.replace(target_funcs, replacement_funcs)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
