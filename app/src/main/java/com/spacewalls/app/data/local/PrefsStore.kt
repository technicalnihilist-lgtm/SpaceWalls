package com.spacewalls.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "spacewalls_prefs")

/** Small key-value store: daily wallpaper, sync bookkeeping and recent searches. */
class PrefsStore(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val DAILY_ID = stringPreferencesKey("daily_wallpaper_id")
        val DAILY_DATE = stringPreferencesKey("daily_wallpaper_date")
        val LAST_SYNC = longPreferencesKey("last_sync_epoch_ms")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    }

    val dailyWallpaperId: Flow<String?> = store.data.map { it[Keys.DAILY_ID] }
    val dailyWallpaperDate: Flow<String?> = store.data.map { it[Keys.DAILY_DATE] }
    val lastSync: Flow<Long> = store.data.map { it[Keys.LAST_SYNC] ?: 0L }
    val recentSearches: Flow<List<String>> = store.data.map { prefs ->
        prefs[Keys.RECENT_SEARCHES]?.split("\u0001")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun setDailyWallpaper(id: String, dateKey: String) {
        store.edit { prefs ->
            prefs[Keys.DAILY_ID] = id
            prefs[Keys.DAILY_DATE] = dateKey
        }
    }

    suspend fun dailySnapshot(): Pair<String?, String?> {
        val prefs: Preferences = store.data.first()
        return prefs[Keys.DAILY_ID] to prefs[Keys.DAILY_DATE]
    }

    suspend fun setLastSync(epochMs: Long) {
        store.edit { it[Keys.LAST_SYNC] = epochMs }
    }

    suspend fun addRecentSearch(term: String) {
        val clean = term.trim()
        if (clean.length < 2) return
        store.edit { prefs ->
            val current = prefs[Keys.RECENT_SEARCHES]
                ?.split("\u0001")
                ?.filter { it.isNotBlank() }
                .orEmpty()
            val updated = (listOf(clean) + current.filterNot { it.equals(clean, true) }).take(10)
            prefs[Keys.RECENT_SEARCHES] = updated.joinToString("\u0001")
        }
    }

    suspend fun clearRecentSearches() {
        store.edit { it.remove(Keys.RECENT_SEARCHES) }
    }
}
