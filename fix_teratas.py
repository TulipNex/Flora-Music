import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target_teratas = """            // Teratas
            if (currentItems.contains("Teratas") && mostPlayed.isNotEmpty()) {
                item {
                    SectionHeader("Teratas", onSeeAll = null)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (isGridView) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(mostPlayed, key = { "top_${it.id}" }) { song ->
                                RecentSongCard(
                                    song = song,
                                    onClick = { onSongClick(song, mostPlayed) }
                                )
                            }
                        }
                    }
                } else {
                    items(mostPlayed, key = { "top_list_${it.id}" }) { song ->
                        HomeSongListRowItem(
                            song = song,
                            isCurrent = false,
                            onClick = { onSongClick(song, mostPlayed) },
                            onAddToPlaylist = { onAddToPlaylist(song) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }"""

replacement_teratas = """            // Teratas
            if (currentItems.contains("Teratas")) {
                item {
                    SectionHeader("Teratas", onSeeAll = null)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (mostPlayed.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text("Belum ada lagu yang sering diputar.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    if (isGridView) {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(mostPlayed, key = { "top_${it.id}" }) { song ->
                                    RecentSongCard(
                                        song = song,
                                        onClick = { onSongClick(song, mostPlayed) }
                                    )
                                }
                            }
                        }
                    } else {
                        items(mostPlayed, key = { "top_list_${it.id}" }) { song ->
                            HomeSongListRowItem(
                                song = song,
                                isCurrent = false,
                                onClick = { onSongClick(song, mostPlayed) },
                                onAddToPlaylist = { onAddToPlaylist(song) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }"""

if target_teratas in content:
    content = content.replace(target_teratas, replacement_teratas)
    print("Fixed Teratas")
else:
    print("Could not find Teratas target")

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
