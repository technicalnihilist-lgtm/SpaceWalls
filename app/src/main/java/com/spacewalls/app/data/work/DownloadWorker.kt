package com.spacewalls.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import com.spacewalls.app.SpaceWallsApp
import com.spacewalls.app.data.local.toWallpaper

/** Background, resumable-in-effect download of one wallpaper. */
class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_WALLPAPER_ID) ?: return Result.failure()
        val container = (applicationContext as SpaceWallsApp).container
        val dao = container.database.wallpaperDao()
        val entity = dao.byId(id) ?: return Result.failure()
        val wallpaper = entity.toWallpaper()

        return runCatching {
            val file = coroutineScope {
                val progressEvents = MutableSharedFlow<Int>(extraBufferCapacity = 64)
                val reporter = launch {
                    progressEvents.collect { percent ->
                        setProgress(workDataOf(KEY_PROGRESS to percent))
                    }
                }
                val downloaded = container.downloadRepository.download(wallpaper) { percent ->
                    progressEvents.tryEmit(percent)
                }
                reporter.cancel()
                downloaded
            }
            dao.setLocalPath(id, file.absolutePath)
            container.downloadRepository.publishToGallery(file, wallpaper.title)
            Result.success(workDataOf(KEY_PATH to file.absolutePath))
        }.getOrElse {
            Result.retry()
        }
    }

    companion object {
        const val KEY_WALLPAPER_ID = "wallpaper_id"
        const val KEY_PROGRESS = "progress"
        const val KEY_PATH = "path"
        fun uniqueName(id: String) = "download_$id"
    }
}
