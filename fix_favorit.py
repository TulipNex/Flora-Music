import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target_favorit = """            // Favorit
            if (currentItems.contains("Favorit") && favorites.isNotEmpty()) {
                item {
                    SectionHeader("Favorit", onSeeAll = null)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (isGridView) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(favorites, key = { "fav_${it.id}" }) { song ->
                                RecentSongCard(
                                    song = song,
                                    onClick = { onSongClick(song, favorites) }
                                )
                            }
                        }
                    }
                } else {
                    items(favorites, key = { "fav_list_${it.id}" }) { song ->
                        HomeSongListRowItem(
                            song = song,
                            isCurrent = false,
                            onClick = { onSongClick(song, favorites) },
                            onAddToPlaylist = { onAddToPlaylist(song) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }"""

replacement_favorit = """            // Favorit
            if (currentItems.contains("Favorit")) {
                item {
                    SectionHeader("Favorit", onSeeAll = null)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (favorites.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text("Belum ada lagu favorit.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    if (isGridView) {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(favorites, key = { "fav_${it.id}" }) { song ->
                                    RecentSongCard(
                                        song = song,
                                        onClick = { onSongClick(song, favorites) }
                                    )
                                }
                            }
                        }
                    } else {
                        items(favorites, key = { "fav_list_${it.id}" }) { song ->
                            HomeSongListRowItem(
                                song = song,
                                isCurrent = false,
                                onClick = { onSongClick(song, favorites) },
                                onAddToPlaylist = { onAddToPlaylist(song) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }"""

if target_favorit in content:
    content = content.replace(target_favorit, replacement_favorit)
    print("Fixed Favorit")
else:
    print("Could not find Favorit target")

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
