package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

object ArtworkCacheManager {
    private const val TAG = "ArtworkCacheManager"
    private const val CACHE_DIR_NAME = "artwork_thumbnails"
    private const val THUMBNAIL_SIZE = 150 // Optimized small resolution for smooth scrolling

    // Get or create the local thumbnail cache directory
    fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    // Generate a unique MD5 key for the song's path to use as a file name
    fun getCacheKey(songPath: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(songPath.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            kotlin.math.abs(songPath.hashCode()).toString()
        }
    }

    // Retrieve the cached tile file if it exists and contains data
    fun getCachedFile(context: Context, songPath: String): File? {
        val key = getCacheKey(songPath)
        val file = File(getCacheDir(context), "$key.jpg")
        return if (file.exists() && file.isFile && file.length() > 0) file else null
    }

    // Pre-extract, rescale, and write to local cache file
    fun cacheArtwork(context: Context, songPath: String, force: Boolean = false): Boolean {
        try {
            val cacheFile = File(getCacheDir(context), "${getCacheKey(songPath)}.jpg")
            if (cacheFile.exists() && !force) {
                return true
            }

            // Extract high-res metadata picture bytes
            val bytes = MusicScanner.getArtworkBytes(songPath) ?: return false
            
            // Decodes with inJustDecodeBounds to pre-sample and save memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            
            options.inSampleSize = calculateInSampleSize(options, THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            options.inJustDecodeBounds = false
            
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return false
            
            // Downscale to target thumbnail size
            val scaledBitmap = if (bitmap.width > THUMBNAIL_SIZE || bitmap.height > THUMBNAIL_SIZE) {
                Bitmap.createScaledBitmap(bitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, true).apply {
                    if (this != bitmap) {
                        bitmap.recycle()
                    }
                }
            } else {
                bitmap
            }

            // Compress to JPEG with 75% quality to minimize file size and disk footprint
            FileOutputStream(cacheFile).use { fos ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos)
            }
            scaledBitmap.recycle()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed pre-extracting artwork for: $songPath", e)
            return false
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
