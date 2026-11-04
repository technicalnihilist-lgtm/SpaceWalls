package com.spacewalls.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spacewalls.app.SpaceWallsApp

/**
 * Keeps the library live: refreshes a rotating set of collections from both
 * APIs and rolls the daily wallpaper forward. Scheduled periodically so the app
 * always opens onto current imagery rather than a frozen snapshot.
 */
class DailyRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as SpaceWallsApp).container
        return runCatching {
            container.repository.refreshLiveContent()
            container.repository.ensureDailyWallpaper()
            container.repository.pruneCache()
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val UNIQUE_NAME = "spacewalls_daily_refresh"
    }
}
