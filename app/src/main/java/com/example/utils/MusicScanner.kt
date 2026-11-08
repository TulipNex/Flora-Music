package com.example.utils

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.domain.model.Song
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.text.Charsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MusicScanner {
    private const val TAG = "MusicScanner"

    // Query raw metadata from MediaStore (extremely fast, no enrichMetadata)
    private fun extractMetadataFromLocalFile(file: File, id: Long): Song {
        val retriever = MediaMetadataRetriever()
        var title = file.nameWithoutExtension
        var artist = "Unknown Artist"
        var album = "Unknown Album"
        var duration = 0L
        try {
            retriever.setDataSource(file.absolutePath)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: title
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: artist
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: album
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting metadata for local file ${file.name}", e)
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            path = file.absolutePath,
            duration = duration,
            size = file.length(),
            dateAdded = file.lastModified() / 1000,
            dateModified = file.lastModified() / 1000
        )
    }

    private fun copyAssetsAndGetSongs(context: Context): List<Song> {
        val assetManager = context.assets
        val mitraDir = File(context.filesDir, "mitra")
        if (!mitraDir.exists()) mitraDir.mkdirs()
        val songs = mutableListOf<Song>()
        try {
            val files = assetManager.list("mitra") ?: return emptyList()
            for ((index, fileName) in files.withIndex()) {
                val ext = fileName.substringAfterLast(".").lowercase()
                if (ext in listOf("mp3", "flac", "m4a", "aac", "ogg", "wav", "opus")) {
                    val destFile = File(mitraDir, fileName)
                    var shouldCopy = !destFile.exists()
                    if (destFile.exists()) {
                        assetManager.open("mitra/$fileName").use { input ->
                            if (destFile.length() != input.available().toLong()) {
                                shouldCopy = true
                            }
                        }
                    }
                    if (shouldCopy) {
                        assetManager.open("mitra/$fileName").use { input ->
                            destFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    // Generate a fake ID based on hash of file name
                    val fakeId = (fileName.hashCode().toLong() and 0xFFFFFFFFL) or 0x1000000000000000L
                    songs.add(extractMetadataFromLocalFile(destFile, fakeId))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying assets", e)
        }
        return songs
    }

    // Query raw metadata from MediaStore (extremely fast, no enrichMetadata)
    suspend fun queryRawSongsFromMediaStore(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val tempSongs = mutableListOf<Song>()
        tempSongs.addAll(copyAssetsAndGetSongs(context))
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA, // File path
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        try {
            contentResolver.query(
                uri,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateAddedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol) ?: "Unknown Song"
                    val artist = c.getString(artistCol) ?: "Unknown Artist"
                    val album = c.getString(albumCol) ?: "Unknown Album"
                    val path = c.getString(dataCol) ?: ""
                    val duration = c.getLong(durationCol)
                    val size = c.getLong(sizeCol)
                    val dateAdded = c.getLong(dateAddedCol)

                    // Only scan existing audio files which end with supported extensions
                    if (path.isNotEmpty() && File(path).exists()) {
                        val ext = path.substringAfterLast(".").lowercase()
                        if (ext in listOf("mp3", "flac", "m4a", "aac", "ogg", "wav", "opus")) {
                            tempSongs.add(
                                Song(
                                    id = id,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    path = path,
                                    duration = duration,
                                    size = size,
                                    dateAdded = dateAdded,
                                    dateModified = c.getLong(dateModifiedCol)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }
        tempSongs
    }

    // Scan local media files using MediaStore
    suspend fun scanLocalStorage(context: Context, onProgress: (scanned: Int, total: Int) -> Unit): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val tempSongs = queryRawSongsFromMediaStore(context)
        val total = tempSongs.size
        for (i in tempSongs.indices) {
            val s = tempSongs[i]
            val enriched = enrichMetadata(s)
            songList.add(enriched)
            onProgress(i + 1, total)
        }
        songList
    }

    // Read full metadata using MediaMetadataRetriever and look for nearby .lrc files
    fun enrichMetadata(song: Song): Song {
        val retriever = MediaMetadataRetriever()
        var trackNumber = 0
        var year = 0
        var genre = "Unknown"
        var albumArtist = "Unknown"
        var composer = "Unknown"
        var embeddedLyrics: String? = null
        var syncedLyrics: String? = null

        try {
            retriever.setDataSource(song.path)
            
            // Extract Track number
            val trackStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            if (!trackStr.isNullOrEmpty()) {
                // Track numbers may be formatted as "1/10", so split and take first
                trackNumber = trackStr.split("/").firstOrNull()?.toIntOrNull() ?: 0
            }

            // Extract Year
            val yearStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            if (!yearStr.isNullOrEmpty()) {
                year = yearStr.toIntOrNull() ?: 0
            }

            // Extract Genre
            val genreStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            if (!genreStr.isNullOrEmpty()) {
                genre = genreStr
            }

            // Extract Album Artist
            val albumArtistStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            if (!albumArtistStr.isNullOrEmpty()) {
                albumArtist = albumArtistStr
            }

            // Extract Composer
            val composerStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
            if (!composerStr.isNullOrEmpty()) {
                composer = composerStr
            }

            // Extract Embedded Lyrics (METADATA_KEY_LYRICS on some platforms is API key 1000)
            val lyricsStr = retriever.extractMetadata(1000)
            if (!lyricsStr.isNullOrEmpty()) {
                embeddedLyrics = lyricsStr
            } else {
                embeddedLyrics = tryExtractEmbeddedLyrics(song.path)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enrich metadata for path: ${song.path}", e)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // ignore
            }
        }

        // Try to read external sidecar .lrc file in same folder
        try {
            val file = File(song.path)
            val parentDir = file.parentFile
            if (parentDir != null) {
                val songBaseName = file.nameWithoutExtension
                val lrcFile = File(parentDir, "$songBaseName.lrc")
                if (lrcFile.exists() && lrcFile.isFile) {
                    val lrcText = lrcFile.readText(Charsets.UTF_8)
                    syncedLyrics = lrcText
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading sidecar .lrc for ${song.title}", e)
        }

        // If no sidecar .lrc but we have embedded lyric, check if it contains timestamps to treat as synced!
        if (syncedLyrics == null && embeddedLyrics != null) {
            if (embeddedLyrics.contains(Regex("\\[\\d{2}:\\d{2}"))) {
                syncedLyrics = embeddedLyrics
            }
        }

        return song.copy(
            trackNumber = trackNumber,
            year = year,
            genre = genre,
            albumArtist = albumArtist,
            composer = composer,
            lyricsEmbedded = embeddedLyrics,
            lyricsSynced = syncedLyrics
        )
    }

    // Read high quality artwork byte array for the track
    fun getArtworkBytes(path: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.embeddedPicture
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (ex: Exception) {
                // ignore
            }
        }
    }

    fun tryExtractEmbeddedLyrics(path: String): String? {
        val file = File(path)
        if (!file.exists() || !file.isFile) return null
        val ext = path.substringAfterLast(".").lowercase()
        return when (ext) {
            "mp3" -> extractId3Lyrics(file)
            "m4a" -> extractM4aLyrics(file)
            "flac" -> extractFlacLyrics(file)
            else -> null
        }
    }

    private fun extractId3Lyrics(file: File): String? {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            val header = ByteArray(10)
            if (fis.read(header) != 10) return null
            if (header[0].toInt() != 0x49 || header[1].toInt() != 0x44 || header[2].toInt() != 0x33) {
                return null
            }
            val version = header[3].toInt() and 0xFF
            if (version < 2 || version > 4) return null
            
            val s0 = header[6].toInt() and 0x7F
            val s1 = header[7].toInt() and 0x7F
            val s2 = header[8].toInt() and 0x7F
            val s3 = header[9].toInt() and 0x7F
            val tagSize = (s0 shl 21) or (s1 shl 14) or (s2 shl 7) or s3
            
            val tagBuffer = ByteArray(tagSize)
            var bytesRead = 0
            while (bytesRead < tagSize) {
                val r = fis.read(tagBuffer, bytesRead, tagSize - bytesRead)
                if (r == -1) break
                bytesRead += r
            }
            
            return findAndParseUslt(tagBuffer, version)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ID3 lyrics", e)
        } finally {
            try { fis?.close() } catch (ex: Exception) {}
        }
        return null
    }

    private fun findAndParseUslt(tagBuffer: ByteArray, version: Int): String? {
        val pattern = "USLT"
        val sig = pattern.toByteArray(Charsets.US_ASCII)
        var idx = 0
        while (idx <= tagBuffer.size - 10) {
            if (tagBuffer[idx] == sig[0] && tagBuffer[idx+1] == sig[1] && 
                tagBuffer[idx+2] == sig[2] && tagBuffer[idx+3] == sig[3]) {
                val f0 = tagBuffer[idx + 4].toInt() and 0xFF
                val f1 = tagBuffer[idx + 5].toInt() and 0xFF
                val f2 = tagBuffer[idx + 6].toInt() and 0xFF
                val f3 = tagBuffer[idx + 7].toInt() and 0xFF
                
                val sizeV3 = (f0 shl 24) or (f1 shl 16) or (f2 shl 8) or f3
                val sizeV4 = (f0 shl 21) or (f1 shl 14) or (f2 shl 7) or f3
                
                val frameSize = if (version == 4) sizeV4 else sizeV3
                
                if (frameSize > 0 && idx + 10 + frameSize <= tagBuffer.size) {
                    val lyrics = parseUsltPayload(tagBuffer, idx + 10, frameSize)
                    if (!lyrics.isNullOrBlank()) {
                        return lyrics
                    }
                }
                
                val altFrameSize = if (version == 4) sizeV3 else sizeV4
                if (altFrameSize > 0 && idx + 10 + altFrameSize <= tagBuffer.size) {
                    val lyrics = parseUsltPayload(tagBuffer, idx + 10, altFrameSize)
                    if (!lyrics.isNullOrBlank()) {
                        return lyrics
                    }
                }
            }
            idx++
        }
        
        val patternV2 = "ULT"
        val sigV2 = patternV2.toByteArray(Charsets.US_ASCII)
        var idxV2 = 0
        while (idxV2 <= tagBuffer.size - 6) {
            if (tagBuffer[idxV2] == sigV2[0] && tagBuffer[idxV2+1] == sigV2[1] && tagBuffer[idxV2+2] == sigV2[2]) {
                val f0 = tagBuffer[idxV2 + 3].toInt() and 0xFF
                val f1 = tagBuffer[idxV2 + 4].toInt() and 0xFF
                val f2 = tagBuffer[idxV2 + 5].toInt() and 0xFF
                val frameSize = (f0 shl 16) or (f1 shl 8) or f2
                if (frameSize > 0 && idxV2 + 6 + frameSize <= tagBuffer.size) {
                    val lyrics = parseUsltPayload(tagBuffer, idxV2 + 6, frameSize)
                    if (!lyrics.isNullOrBlank()) {
                        return lyrics
                    }
                }
            }
            idxV2++
        }
        return null
    }

    private fun parseUsltPayload(buffer: ByteArray, startOffset: Int, size: Int): String? {
        if (size <= 4) return null
        val encodingByte = buffer[startOffset].toInt() and 0xFF
        var descOffset = startOffset + 4
        val endOfFrame = startOffset + size
        
        if (encodingByte == 1 || encodingByte == 2) {
            var i = descOffset
            while (i + 1 < endOfFrame) {
                if (buffer[i].toInt() == 0 && buffer[i + 1].toInt() == 0) {
                    descOffset = i + 2
                    break
                }
                i += 2
            }
        } else {
            var i = descOffset
            while (i < endOfFrame) {
                if (buffer[i].toInt() == 0) {
                    descOffset = i + 1
                    break
                }
                i++
            }
        }
        if (descOffset >= endOfFrame) return null
        val lyricsLen = endOfFrame - descOffset
        if (lyricsLen <= 0) return null
        return try {
            when (encodingByte) {
                0 -> String(buffer, descOffset, lyricsLen, Charsets.ISO_8859_1).trim()
                1 -> String(buffer, descOffset, lyricsLen, Charsets.UTF_16).trim()
                2 -> String(buffer, descOffset, lyricsLen, Charsets.UTF_16BE).trim()
                3 -> String(buffer, descOffset, lyricsLen, Charsets.UTF_8).trim()
                else -> String(buffer, descOffset, lyricsLen, Charsets.UTF_8).trim()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractM4aLyrics(file: File): String? {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            val channel = fis.channel
            val size = channel.size()
            return parseMp4Atoms(channel, 0, size)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing M4A lyrics", e)
        } finally {
            try { fis?.close() } catch (ex: Exception) {}
        }
        return null
    }

    private fun parseMp4Atoms(channel: FileChannel, start: Long, limit: Long): String? {
        var offset = start
        val headerBuffer = ByteBuffer.allocate(8)
        while (offset + 8 <= limit) {
            channel.position(offset)
            headerBuffer.clear()
            if (channel.read(headerBuffer) != 8) break
            headerBuffer.flip()
            
            val sizeVal = headerBuffer.getInt().toLong() and 0xFFFFFFFFL
            val typeBytes = ByteArray(4)
            headerBuffer.get(typeBytes)
            val type = String(typeBytes, Charsets.ISO_8859_1)
            
            val atomSize = if (sizeVal == 1L) {
                val sizeBuf = ByteBuffer.allocate(8)
                channel.read(sizeBuf)
                sizeBuf.flip()
                sizeBuf.getLong()
            } else if (sizeVal == 0L) {
                limit - offset
            } else {
                sizeVal
            }
            
            val headerLen = if (sizeVal == 1L) 16 else 8
            val contentStart = offset + headerLen
            val contentSize = atomSize - headerLen
            if (atomSize <= 0) break
            
            if (type == "moov" || type == "udta" || type == "ilst" || type == "©lyr") {
                val res = parseMp4Atoms(channel, contentStart, contentStart + contentSize)
                if (res != null) return res
            } else if (type == "meta") {
                val res = parseMp4Atoms(channel, contentStart + 4, contentStart + contentSize)
                if (res != null) return res
            } else if (type == "data") {
                if (contentSize > 8) {
                    channel.position(contentStart)
                    val dataBuffer = ByteBuffer.allocate(contentSize.toInt())
                    channel.read(dataBuffer)
                    dataBuffer.flip()
                    val dataType = dataBuffer.getInt()
                    dataBuffer.get() // skip locale
                    dataBuffer.get()
                    dataBuffer.get()
                    dataBuffer.get()
                    val lyricsBytes = ByteArray(dataBuffer.remaining())
                    dataBuffer.get(lyricsBytes)
                    return String(lyricsBytes, Charsets.UTF_8).trim()
                }
            }
            offset += atomSize
        }
        return null
    }

    private fun extractFlacLyrics(file: File): String? {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            val header = ByteArray(4)
            if (fis.read(header) != 4) return null
            if (String(header, Charsets.US_ASCII) != "fLaC") return null
            
            var isLast = false
            while (!isLast) {
                val blockHeader = ByteArray(4)
                if (fis.read(blockHeader) != 4) break
                isLast = (blockHeader[0].toInt() and 0x80) != 0
                val blockType = blockHeader[0].toInt() and 0x7F
                val length = ((blockHeader[1].toInt() and 0xFF) shl 16) or
                             ((blockHeader[2].toInt() and 0xFF) shl 8) or
                             (blockHeader[3].toInt() and 0xFF)
                             
                if (blockType == 4) {
                    val payload = ByteArray(length)
                    if (fis.read(payload) != length) break
                    val pBuf = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
                    
                    if (pBuf.remaining() < 4) break
                    val vendorLen = pBuf.getInt()
                    if (pBuf.remaining() < vendorLen) break
                    pBuf.position(pBuf.position() + vendorLen)
                    
                    if (pBuf.remaining() < 4) break
                    val commentCount = pBuf.getInt()
                    for (c in 0 until commentCount) {
                        if (pBuf.remaining() < 4) break
                        val commentLen = pBuf.getInt()
                        if (pBuf.remaining() < commentLen) break
                        val commentBytes = ByteArray(commentLen)
                        pBuf.get(commentBytes)
                        val commentStr = String(commentBytes, Charsets.UTF_8)
                        
                        val eqIndex = commentStr.indexOf('=')
                        if (eqIndex != -1) {
                            val key = commentStr.substring(0, eqIndex).uppercase()
                            val value = commentStr.substring(eqIndex + 1)
                            if (key == "LYRICS" || key == "UNSYNCED LYRICS" || key == "UNSYNCEDLYRICS") {
                                return value.trim()
                            }
                        }
                    }
                    break
                } else {
                    val skipped = fis.skip(length.toLong())
                    if (skipped < length) break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing FLAC lyrics", e)
        } finally {
            try { fis?.close() } catch (ex: Exception) {}
        }
        return null
    }
}
