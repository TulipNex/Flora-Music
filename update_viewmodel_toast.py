import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

target = """                        audioFile.commit()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                repository.updateSong(updated)
                playerController.updateCurrentSongReference(updated)
            }
        }
    }"""

replacement = """                        audioFile.commit()
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(getApplication(), "Metadata file berhasil diperbarui", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(getApplication(), "Gagal mengubah file fisik: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                repository.updateSong(updated)
                playerController.updateCurrentSongReference(updated)
            }
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
