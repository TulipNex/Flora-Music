import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """                TextButton(
                    onClick = {
                        viewModel.updateSongMetadata(
                            songId = song.id,
                            newTitle = title,
                            newArtist = artist,
                            newAlbum = album,
                            newLyrics = lyrics,
                            newComposer = composer,
                            newGenre = genre,
                            newWriter = writer,
                            newTrackNumber = trackNumber.toIntOrNull() ?: 0,
                            newDiscNumber = discNumber,
                            newYear = year.toIntOrNull() ?: 0,
                            newComment = comment,
                            newCustomCoverUri = customCoverUri?.toString()
                        )
                        onDismiss()
                    }
                ) {"""

replacement = """                val manageStorageLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        if (android.os.Environment.isExternalStorageManager()) {
                            viewModel.updateSongMetadata(
                                songId = song.id,
                                newTitle = title,
                                newArtist = artist,
                                newAlbum = album,
                                newLyrics = lyrics,
                                newComposer = composer,
                                newGenre = genre,
                                newWriter = writer,
                                newTrackNumber = trackNumber.toIntOrNull() ?: 0,
                                newDiscNumber = discNumber,
                                newYear = year.toIntOrNull() ?: 0,
                                newComment = comment,
                                newCustomCoverUri = customCoverUri?.toString()
                            )
                            onDismiss()
                        } else {
                            android.widget.Toast.makeText(contextLocal, "Izin diperlukan untuk mengedit file fisik.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                TextButton(
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && !android.os.Environment.isExternalStorageManager()) {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                intent.data = android.net.Uri.parse("package:${contextLocal.packageName}")
                                manageStorageLauncher.launch(intent)
                            } catch (e: Exception) {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                manageStorageLauncher.launch(intent)
                            }
                        } else {
                            viewModel.updateSongMetadata(
                                songId = song.id,
                                newTitle = title,
                                newArtist = artist,
                                newAlbum = album,
                                newLyrics = lyrics,
                                newComposer = composer,
                                newGenre = genre,
                                newWriter = writer,
                                newTrackNumber = trackNumber.toIntOrNull() ?: 0,
                                newDiscNumber = discNumber,
                                newYear = year.toIntOrNull() ?: 0,
                                newComment = comment,
                                newCustomCoverUri = customCoverUri?.toString()
                            )
                            onDismiss()
                        }
                    }
                ) {"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
