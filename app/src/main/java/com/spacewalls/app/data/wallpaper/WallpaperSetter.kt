package com.spacewalls.app.data.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.model.WallpaperTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Applies a wallpaper through the platform [WallpaperManager], decoding with a
 * sample size matched to the device screen so full-resolution files never blow
 * up the heap.
 */
class WallpaperSetter(
    private val context: Context,
    private val client: OkHttpClient
) {

    suspend fun apply(wallpaper: Wallpaper, target: WallpaperTarget): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val manager = WallpaperManager.getInstance(context)
                val bitmap = decodeForScreen(wallpaper)
                    ?: throw IOException("Could not decode the image")

                try {
                    when (target) {
                        WallpaperTarget.HOME -> manager.setBitmap(bitmap)
                        WallpaperTarget.LOCK -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                            } else {
                                manager.setBitmap(bitmap)
                            }
                        }
                        WallpaperTarget.BOTH -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                manager.setBitmap(
                                    bitmap,
                                    null,
                                    true,
                                    WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                                )
                            } else {
                                manager.setBitmap(bitmap)
                            }
                        }
                    }
                } finally {
                    if (!bitmap.isRecycled) bitmap.recycle()
                }
                Unit
            }
        }

    private fun decodeForScreen(wallpaper: Wallpaper): Bitmap? {
        val local = wallpaper.localPath?.let(::File)?.takeIf { it.exists() }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        if (local != null) {
            BitmapFactory.decodeFile(local.absolutePath, bounds)
        } else {
            openStream(wallpaper)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
        }

        val metrics = context.resources.displayMetrics
        val targetWidth = maxOf(metrics.widthPixels, metrics.heightPixels)
        val targetHeight = minOf(metrics.widthPixels, metrics.heightPixels)

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, targetWidth, targetHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return if (local != null) {
            BitmapFactory.decodeFile(local.absolutePath, options)
        } else {
            openStream(wallpaper)?.use { BitmapFactory.decodeStream(it, null, options) }
        }
    }

    private fun openStream(wallpaper: Wallpaper) = runCatching {
        val request = Request.Builder().url(wallpaper.originalUrl).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
        response.body?.byteStream()
    }.getOrNull()

    private fun sampleSize(width: Int, height: Int, targetWidth: Int, targetHeight: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sample = 1
        while (width / (sample * 2) >= targetWidth && height / (sample * 2) >= targetHeight) {
            sample *= 2
        }
        return sample
    }
}
