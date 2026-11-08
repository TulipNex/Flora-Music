import re

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'r') as f:
    content = f.read()

import_str = """import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File"""

content = content.replace("import org.jaudiotagger.tag.FieldKey\nimport java.io.File", import_str)

target_art = """                        if (updated.comment.isNotBlank()) tag.setField(FieldKey.COMMENT, updated.comment)"""
replacement_art = """                        if (updated.comment.isNotBlank()) tag.setField(FieldKey.COMMENT, updated.comment)
                        
                        if (updated.customCoverUri != null) {
                            try {
                                val uri = android.net.Uri.parse(updated.customCoverUri)
                                getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                                    val bytes = inputStream.readBytes()
                                    val artwork = ArtworkFactory.getNew()
                                    artwork.binaryData = bytes
                                    artwork.mimeType = "image/jpeg"
                                    tag.deleteArtworkField()
                                    tag.setField(artwork)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }"""

content = content.replace(target_art, replacement_art)

with open('app/src/main/java/com/example/presentation/MusicViewModel.kt', 'w') as f:
    f.write(content)

