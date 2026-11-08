import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

target_genre = """            // Genre
            if (currentItems.contains("Genre") && uniqueGenresWithSong.isNotEmpty()) {
                item {
                    SectionHeader("Genre", onSeeAll = null)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (isGridView) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(uniqueGenresWithSong.keys.toList(), key = { "genre_$it" }) { genre ->
                                GenreGridCell(
                                    genreName = genre,
                                    songs = uniqueGenresWithSong[genre] ?: emptyList(),
                                    onClick = { onGenreSelect(genre) }
                                )
                            }
                        }
                    }
                } else {
                    items(uniqueGenresWithSong.keys.toList(), key = { "genre_list_$it" }) { genre ->
                        HomeGenreListRowItem(
                            genreName = genre,
                            songs = uniqueGenresWithSong[genre] ?: emptyList(),
                            onClick = { onGenreSelect(genre) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }"""

replacement_genre = """            // Genre
            if (currentItems.contains("Genre")) {
                item {
                    SectionHeader("Genre", onSeeAll = null)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (uniqueGenresWithSong.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text("Genre kosong. Pindai musik lokal Anda.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    if (isGridView) {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(uniqueGenresWithSong.keys.toList(), key = { "genre_$it" }) { genre ->
                                    GenreGridCell(
                                        genreName = genre,
                                        songs = uniqueGenresWithSong[genre] ?: emptyList(),
                                        onClick = { onGenreSelect(genre) }
                                    )
                                }
                            }
                        }
                    } else {
                        items(uniqueGenresWithSong.keys.toList(), key = { "genre_list_$it" }) { genre ->
                            HomeGenreListRowItem(
                                genreName = genre,
                                songs = uniqueGenresWithSong[genre] ?: emptyList(),
                                onClick = { onGenreSelect(genre) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }"""

if target_genre in content:
    content = content.replace(target_genre, replacement_genre)
    print("Fixed Genre")
else:
    print("Could not find Genre target")

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
