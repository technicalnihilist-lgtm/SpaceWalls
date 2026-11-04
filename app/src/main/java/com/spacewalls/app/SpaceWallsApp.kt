package com.spacewalls.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.spacewalls.app.data.work.WorkScheduler
import com.spacewalls.app.di.AppContainer
import java.io.File

class SpaceWallsApp : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Never let background scheduling take the app down on launch.
        runCatching { WorkScheduler.scheduleLiveRefresh(this) }
    }

    /**
     * Coil is configured for big source images: a generous disk cache for
     * thumbnails plus a memory cache sized to the device.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.22)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(File(cacheDir, "image_cache"))
                .maxSizeBytes(192L * 1024 * 1024)
                .build()
        }
        .crossfade(200)
        .respectCacheHeaders(false)
        .build()
}
