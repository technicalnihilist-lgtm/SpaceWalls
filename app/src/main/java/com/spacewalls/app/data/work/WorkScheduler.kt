package com.spacewalls.app.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private val networkConstraint =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    /** Refreshes imagery every six hours while the device is charging or idle-safe. */
    fun scheduleLiveRefresh(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyRefreshWorker>(6, TimeUnit.HOURS)
            .setConstraints(networkConstraint)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyRefreshWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Queues one download; the unique name prevents duplicate work per image. */
    fun enqueueDownload(context: Context, wallpaperId: String) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(networkConstraint)
            .setInputData(workDataOf(DownloadWorker.KEY_WALLPAPER_ID to wallpaperId))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DownloadWorker.uniqueName(wallpaperId),
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun observeDownload(context: Context, wallpaperId: String): Flow<List<WorkInfo>> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(DownloadWorker.uniqueName(wallpaperId))

    fun cancelDownload(context: Context, wallpaperId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(DownloadWorker.uniqueName(wallpaperId))
    }
}
