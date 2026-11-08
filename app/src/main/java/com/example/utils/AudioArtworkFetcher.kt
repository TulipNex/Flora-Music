package com.example.utils

import android.content.Context
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer
import okio.source
import okio.buffer
import java.io.File

data class AudioArtwork(val path: String, val customCoverUri: String? = null, val dateModified: Long = 0L)

class AudioArtworkFetcher(
    private val artwork: AudioArtwork,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        if (!artwork.customCoverUri.isNullOrEmpty()) {
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
        val cachedFile = ArtworkCacheManager.getCachedFile(options.context, artwork.path)
        if (cachedFile != null) {
            try {
                val source = cachedFile.source().buffer()
                return SourceResult(
                    source = ImageSource(source = source, context = options.context),
                    mimeType = "image/jpeg",
                    dataSource = DataSource.DISK
                )
            } catch (e: Exception) {
                // fall through to extract raw bytes if cached file stream fails
            }
        }

        val bytes = MusicScanner.getArtworkBytes(artwork.path) ?: return null
        val buffer = Buffer().write(bytes)
        return SourceResult(
            source = ImageSource(source = buffer, context = options.context),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<AudioArtwork> {
        override fun create(data: AudioArtwork, options: Options, imageLoader: ImageLoader): Fetcher? {
            return AudioArtworkFetcher(data, options)
        }
    }
}
