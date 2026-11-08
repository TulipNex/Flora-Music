import re

with open('app/src/main/java/com/example/utils/AudioArtworkFetcher.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "data class AudioArtwork(val path: String)",
    "data class AudioArtwork(val path: String, val customCoverUri: String? = null)"
)

target_fetch = """        val cachedFile = ArtworkCacheManager.getCachedFile(options.context, artwork.path)"""

replacement_fetch = """        if (!artwork.customCoverUri.isNullOrEmpty()) {
            try {
                val uri = android.net.Uri.parse(artwork.customCoverUri)
                val inputStream = options.context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val buffer = okio.Buffer().readFrom(inputStream)
                    inputStream.close()
                    return SourceResult(
                        source = ImageSource(source = buffer, context = options.context),
                        mimeType = null,
                        dataSource = DataSource.DISK
                    )
                }
            } catch (e: Exception) {
                // fall through
            }
        }
        val cachedFile = ArtworkCacheManager.getCachedFile(options.context, artwork.path)"""

content = content.replace(target_fetch, replacement_fetch)

with open('app/src/main/java/com/example/utils/AudioArtworkFetcher.kt', 'w') as f:
    f.write(content)
