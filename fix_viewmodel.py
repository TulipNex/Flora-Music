import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

import_statement = """import org.json.JSONObject
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File"""

content = content.replace("import org.json.JSONObject", import_statement)

target_update = """                val updated = currentSong.copy(
                    title = newTitle,
                    artist = newArtist,
                    album = newAlbum,
                    lyricsEmbedded = newLyrics.takeIf { it.isNotBlank() },
                    composer = newComposer,
                    genre = newGenre,
                    writer = newWriter,
                    trackNumber = newTrackNumber,
                    discNumber = newDiscNumber,
                    year = newYear,
                    comment = newComment,
                    customCoverUri = newCustomCoverUri
                )
                repository.updateSong(updated)
                playerController.updateCurrentSongReference(updated)"""

replacement_update = """                val updated = currentSong.copy(
                    title = newTitle,
                    artist = newArtist,
                    album = newAlbum,
                    lyricsEmbedded = newLyrics.takeIf { it.isNotBlank() },
                    composer = newComposer,
                    genre = newGenre,
                    writer = newWriter,
                    trackNumber = newTrackNumber,
                    discNumber = newDiscNumber,
                    year = newYear,
                    comment = newComment,
                    customCoverUri = newCustomCoverUri
                )

                // Try to write to the physical file using Jaudiotagger
                try {
                    val f = File(updated.path)
                    if (f.exists() && f.canWrite()) {
                        val audioFile = AudioFileIO.read(f)
                        val tag = audioFile.tagOrCreateAndSetDefault
                        
                        tag.setField(FieldKey.TITLE, updated.title)
                        tag.setField(FieldKey.ARTIST, updated.artist)
                        tag.setField(FieldKey.ALBUM, updated.album)
                        tag.setField(FieldKey.LYRICS, updated.lyricsEmbedded ?: "")
                        tag.setField(FieldKey.COMPOSER, updated.composer)
                        tag.setField(FieldKey.GENRE, updated.genre)
                        
                        if (updated.trackNumber > 0) tag.setField(FieldKey.TRACK, updated.trackNumber.toString())
                        if (updated.discNumber.isNotBlank()) tag.setField(FieldKey.DISC_NO, updated.discNumber)
                        if (updated.year > 0) tag.setField(FieldKey.YEAR, updated.year.toString())
                        if (updated.comment.isNotBlank()) tag.setField(FieldKey.COMMENT, updated.comment)
                        
                        audioFile.commit()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                repository.updateSong(updated)
                playerController.updateCurrentSongReference(updated)"""

content = content.replace(target_update, replacement_update)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)
