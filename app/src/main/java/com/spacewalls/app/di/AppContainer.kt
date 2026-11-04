package com.spacewalls.app.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.spacewalls.app.data.local.PrefsStore
import com.spacewalls.app.data.local.SpaceWallsDatabase
import com.spacewalls.app.data.remote.HttpClients
import com.spacewalls.app.data.remote.NetworkMonitor
import com.spacewalls.app.data.repo.DownloadRepository
import com.spacewalls.app.data.repo.WallpaperRepository
import com.spacewalls.app.data.wallpaper.WallpaperSetter

/**
 * Hand-rolled dependency container. Small enough that a DI framework would only
 * add build weight, and explicit enough to see every dependency at a glance.
 */
class AppContainer(context: Context) {

    val appContext: Context = context.applicationContext

    val http by lazy { HttpClients(appContext) }
    val database by lazy { SpaceWallsDatabase.build(appContext) }
    val prefs by lazy { PrefsStore(appContext) }
    val networkMonitor by lazy { NetworkMonitor(appContext) }

    val repository by lazy {
        WallpaperRepository(
            wikimediaApi = http.wikimedia,
            nasaApi = http.nasa,
            dao = database.wallpaperDao(),
            network = networkMonitor,
            prefs = prefs
        )
    }

    val downloadRepository by lazy { DownloadRepository(appContext, http.okHttp) }
    val wallpaperSetter by lazy { WallpaperSetter(appContext, http.okHttp) }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer was not provided")
}
