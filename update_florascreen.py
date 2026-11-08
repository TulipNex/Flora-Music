import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

content = content.replace("enum class FloraScreen {\n    HOME, SONGS, PLAYLISTS, ALBUMS, ARTISTS, SEARCH, SETTINGS, TAB_SETTINGS\n}", "enum class FloraScreen {\n    HOME, SONGS, PLAYLISTS, ALBUMS, ARTISTS, SEARCH, SETTINGS, TAB_SETTINGS, LYRICS_SETTINGS\n}")

target_route = """                            FloraScreen.TAB_SETTINGS -> TabSettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { currentScreen = FloraScreen.SETTINGS }
                            )
                            else -> Box(modifier = Modifier.fillMaxSize())"""

replacement_route = """                            FloraScreen.TAB_SETTINGS -> TabSettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { currentScreen = FloraScreen.SETTINGS }
                            )
                            FloraScreen.LYRICS_SETTINGS -> LyricsSettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { currentScreen = FloraScreen.SETTINGS }
                            )
                            else -> Box(modifier = Modifier.fillMaxSize())"""
                            
content = content.replace(target_route, replacement_route)

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
