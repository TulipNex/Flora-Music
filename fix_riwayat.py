import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target_riwayat = """            // Riwayat
            if (currentItems.contains("Riwayat") && recentlyPlayed.isNotEmpty()) {
                item {
                    SectionHeader("Riwayat", onSeeAll = null)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (isGridView) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(recentlyPlayed, key = { "hist_${it.id}" }) { song ->
                                RecentSongCard(
                                    song = song,
                                    onClick = { onSongClick(song, recentlyPlayed) }
                                )
                            }
                        }
                    }
                } else {
                    items(recentlyPlayed, key = { "hist_list_${it.id}" }) { song ->
                        HomeSongListRowItem(
                            song = song,
                            isCurrent = false,
                            onClick = { onSongClick(song, recentlyPlayed) },
                            onAddToPlaylist = { onAddToPlaylist(song) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }"""

replacement_riwayat = """            // Riwayat
            if (currentItems.contains("Riwayat")) {
                item {
                    SectionHeader("Riwayat", onSeeAll = null)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (recentlyPlayed.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat putar.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    if (isGridView) {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(recentlyPlayed, key = { "hist_${it.id}" }) { song ->
                                    RecentSongCard(
                                        song = song,
                                        onClick = { onSongClick(song, recentlyPlayed) }
                                    )
                                }
                            }
                        }
                    } else {
                        items(recentlyPlayed, key = { "hist_list_${it.id}" }) { song ->
                            HomeSongListRowItem(
                                song = song,
                                isCurrent = false,
                                onClick = { onSongClick(song, recentlyPlayed) },
                                onAddToPlaylist = { onAddToPlaylist(song) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }"""

if target_riwayat in content:
    content = content.replace(target_riwayat, replacement_riwayat)
    print("Fixed Riwayat")
else:
    print("Could not find Riwayat target")

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
