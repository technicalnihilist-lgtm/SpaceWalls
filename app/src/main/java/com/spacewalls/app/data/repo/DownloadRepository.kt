package com.spacewalls.app.data.repo

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.spacewalls.app.data.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Real downloads with duplicate prevention: a wallpaper is fetched once, stored
 * in app-private storage for offline use, and copied into the device gallery so
 * the user owns the file.
 */
class DownloadRepository(
    private val context: Context,
    private val client: OkHttpClient
) {

    private val dir: File
        get() = File(context.filesDir, "wallpapers").apply { if (!exists()) mkdirs() }

    fun fileFor(wallpaper: Wallpaper): File {
        val extension = when {
            wallpaper.mimeType.contains("png") -> "png"
            wallpaper.mimeType.contains("webp") -> "webp"
            else -> "jpg"
        }
        return File(dir, "${wallpaper.id.replace(':', '_')}.$extension")
    }

    fun cachedFile(wallpaper: Wallpaper): File? {
        val file = fileFor(wallpaper)
        return if (file.exists() && file.length() > 0) file else null
    }

    /**
     * Streams the full-resolution file to disk. Returns immediately if the file
     * already exists, which is what prevents duplicate downloads.
     */
    suspend fun download(
        wallpaper: Wallpaper,
        onProgress: (Int) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        cachedFile(wallpaper)?.let { existing ->
            onProgress(100)
            return@withContext existing
        }

        val target = fileFor(wallpaper)
        val partial = File(target.absolutePath + ".part")
        if (partial.exists()) partial.delete()

        val request = Request.Builder().url(wallpaper.originalUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Download failed: HTTP ${response.code}")
            val body = response.body ?: throw IOException("Empty response body")
            val total = body.contentLength()

            body.byteStream().use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var written = 0L
                    var lastPercent = -1
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        written += read
                        if (total > 0) {
                            val percent = ((written * 100) / total).toInt().coerceIn(0, 100)
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress(percent)
                            }
                        }
                    }
                    output.flush()
                }
            }
        }

        if (partial.length() <= 0L) {
            partial.delete()
            throw IOException("Downloaded file was empty")
        }
        if (target.exists()) target.delete()
        if (!partial.renameTo(target)) throw IOException("Could not finalise download")
        onProgress(100)
        target
    }

    /**
     * Copies a downloaded file into the shared gallery (Android 10+) or the
     * app's external folder on older releases.
     */
    suspend fun publishToGallery(file: File, title: String): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val safeName = title.replace(Regex("[^A-Za-z0-9 _-]"), "_").trim()
                .ifBlank { "spacewalls" }
                .take(60)
            val displayName = "SpaceWalls_$safeName.${file.extension}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeFor(file))
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SpaceWalls")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@runCatching null
                resolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { input -> input.copyTo(out) }
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } else {
                val pictures = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "SpaceWalls"
                ).apply { mkdirs() }
                val destination = File(pictures, displayName)
                file.copyTo(destination, overwrite = true)
                Uri.fromFile(destination)
            }
        }.getOrNull()
    }

    fun deleteLocal(wallpaper: Wallpaper): Boolean {
        val file = fileFor(wallpaper)
        return file.exists() && file.delete()
    }

    private fun mimeFor(file: File): String = when (file.extension.lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "image/jpeg"
    }
}
