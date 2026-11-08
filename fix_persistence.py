import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

# Update saveTabConfigs to also save hidden modules
save_tab_configs = """
    fun saveTabConfigs(configs: List<TabConfig>, hidden: List<String>) {
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
    }
"""

content = re.sub(r'fun saveTabConfigs\(configs: List<TabConfig>, hidden: List<String>\) \{.*?\n    \}', save_tab_configs.strip(), content, flags=re.DOTALL)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
