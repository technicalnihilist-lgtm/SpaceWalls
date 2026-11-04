package com.spacewalls.app.data.repo

import com.spacewalls.app.data.local.PrefsStore
import com.spacewalls.app.data.local.WallpaperDao
import com.spacewalls.app.data.local.WallpaperEntity
import com.spacewalls.app.data.local.toEntity
import com.spacewalls.app.data.local.toWallpaper
import com.spacewalls.app.data.mapper.WallpaperMappers
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.remote.NetworkMonitor
import com.spacewalls.app.data.remote.nasa.NasaApi
import com.spacewalls.app.data.remote.wikimedia.WikimediaApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * The single place where remote space imagery and the local mirror meet.
 *
 * Browsing is always live-first: every page pulls fresh results from both
 * providers, verifies their rights, de-duplicates them, then caches the merged
 * page so the same content stays browsable offline.
 */
class WallpaperRepository(
    private val wikimediaApi: WikimediaApi,
    private val nasaApi: NasaApi,
    private val dao: WallpaperDao,
    private val network: NetworkMonitor,
    private val prefs: PrefsStore
) {

    data class LoadOutcome(
        val items: List<Wallpaper>,
        val hasMore: Boolean,
        val offline: Boolean,
        val warnings: List<String>
    )

    fun observeCached(queryKey: String): Flow<List<Wallpaper>> =
        dao.observeQuery(queryKey).map { list -> list.map(WallpaperEntity::toWallpaper) }

    fun observeFavorites(): Flow<List<Wallpaper>> =
        dao.observeFavorites().map { list -> list.map(WallpaperEntity::toWallpaper) }

    fun observeDownloads(): Flow<List<Wallpaper>> =
        dao.observeDownloaded().map { list -> list.map(WallpaperEntity::toWallpaper) }

    fun observeRecentlyViewed(limit: Int = 20): Flow<List<Wallpaper>> =
        dao.observeRecentlyViewed(limit).map { list -> list.map(WallpaperEntity::toWallpaper) }

    fun observeRecentlyAdded(limit: Int = 20): Flow<List<Wallpaper>> {
        val since = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return dao.observeRecentlyAdded(since, limit)
            .map { list -> list.map(WallpaperEntity::toWallpaper) }
    }

    fun observeAll(): Flow<List<Wallpaper>> =
        dao.observeAll().map { list -> list.map(WallpaperEntity::toWallpaper) }

    fun observeWallpaper(id: String): Flow<Wallpaper?> =
        dao.observeById(id).map { it?.toWallpaper() }

    suspend fun wallpaper(id: String): Wallpaper? = dao.byId(id)?.toWallpaper()

    /** Live-first: the UI uses this only to explain a failure, never to skip a fetch. */
    fun isOnline(): Boolean = network.currentlyOnline()

    suspend fun setFavorite(id: String, favorite: Boolean) = dao.setFavorite(id, favorite)

    suspend fun markViewed(id: String) = dao.markViewed(id, System.currentTimeMillis())

    suspend fun searchLocal(term: String, limit: Int = 60): List<Wallpaper> =
        dao.searchLocal(term.trim(), limit).map(WallpaperEntity::toWallpaper)

    /**
     * Loads one page of a collection from both providers in parallel.
     * A failure on one provider still returns the other's results.
     */
    suspend fun loadPage(
        queryKey: String,
        categoryKey: String,
        wikimediaQuery: String,
        nasaQuery: String,
        page: Int,
        pageSize: Int = PAGE_SIZE
    ): LoadOutcome {
        val warnings = mutableListOf<String>()

        val results = coroutineScope {
            val wikimedia = async {
                runCatching {
                    wikimediaApi.searchImages(
                        search = "filetype:bitmap $wikimediaQuery",
                        offset = (page - 1) * pageSize,
                        limit = pageSize
                    )
                }
            }
            val nasa = async {
                runCatching { nasaApi.search(query = nasaQuery, page = page, pageSize = pageSize) }
            }

            val fromCommons = wikimedia.await().fold(
                onSuccess = { response ->
                    if (response.error != null) {
                        warnings += "Wikimedia: ${response.error.info ?: response.error.code ?: "error"}"
                        emptyList()
                    } else {
                        response.query?.pages.orEmpty().mapNotNull(WallpaperMappers::fromWikimedia)
                    }
                },
                onFailure = {
                    warnings += "Wikimedia Commons unreachable."
                    emptyList()
                }
            )

            val fromNasa = nasa.await().fold(
                onSuccess = { response ->
                    response.collection?.items.orEmpty().mapNotNull(WallpaperMappers::fromNasa)
                },
                onFailure = {
                    warnings += "NASA library unreachable."
                    emptyList()
                }
            )
            fromCommons to fromNasa
        }

        val merged = interleave(results.first, results.second)
            .distinctBy { it.id }
            .distinctBy { it.title.lowercase(Locale.ROOT).trim() }

        if (merged.isEmpty()) {
            // Both providers failed: fall back to whatever is already cached so
            // the screen is never empty, and say so plainly.
            val cached = cachedFor(queryKey, categoryKey)
            return LoadOutcome(
                items = cached,
                hasMore = false,
                offline = true,
                warnings = warnings + "Showing cached imagery — live sources unreachable."
            )
        }

        val existing = dao.byIds(merged.map { it.id }).associateBy { it.id }
        dao.upsertAll(
            merged.map {
                it.toEntity(
                    queryKey = queryKey,
                    categoryKey = categoryKey,
                    existing = existing[it.id]
                )
            }
        )

        val hasMore = results.first.size >= pageSize || results.second.size >= pageSize
        return LoadOutcome(merged, hasMore, offline = false, warnings = warnings)
    }

    /** Cached rows for a collection, newest first — the offline fallback. */
    suspend fun cachedFor(queryKey: String, categoryKey: String): List<Wallpaper> {
        val byQuery = dao.searchLocal(queryKey, 200).filter { it.queryKey == queryKey }
        val rows = if (byQuery.isNotEmpty()) {
            byQuery
        } else {
            dao.searchLocal(categoryKey, 200).filter { it.categoryKey == categoryKey }
        }
        return rows.map(WallpaperEntity::toWallpaper)
    }

    private fun interleave(a: List<Wallpaper>, b: List<Wallpaper>): List<Wallpaper> {
        val out = ArrayList<Wallpaper>(a.size + b.size)
        val max = maxOf(a.size, b.size)
        for (i in 0 until max) {
            a.getOrNull(i)?.let(out::add)
            b.getOrNull(i)?.let(out::add)
        }
        return out
    }

    /**
     * Chooses today's wallpaper deterministically from the cached library so it
     * changes every day without repeating, and never depends on a hard-coded list.
     */
    suspend fun ensureDailyWallpaper(): Wallpaper? {
        val today = dayKey()
        val (cachedId, cachedDate) = prefs.dailySnapshot()
        if (cachedDate == today && cachedId != null) {
            dao.byId(cachedId)?.let { return it.toWallpaper() }
        }

        var pool = dao.searchLocal("", 400).map(WallpaperEntity::toWallpaper)
        if (pool.size < 6) {
            loadPage(
                queryKey = "daily",
                categoryKey = "collections",
                wikimediaQuery = "nebula deep space",
                nasaQuery = "nebula",
                page = 1,
                pageSize = 20
            )
            pool = dao.searchLocal("", 400).map(WallpaperEntity::toWallpaper)
        }
        if (pool.isEmpty()) return null

        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val chosen = pool[dayOfYear % pool.size]
        prefs.setDailyWallpaper(chosen.id, today)
        return chosen
    }

    private data class SectionSeed(
        val key: String,
        val category: String,
        val wikimediaQuery: String,
        val nasaQuery: String
    )

    private val liveSections = listOf(
        SectionSeed("featured", "collections", "nebula galaxy", "nebula"),
        SectionSeed("trending", "galaxies", "spiral galaxy", "galaxy"),
        SectionSeed("planets", "planets", "planet solar system", "planet"),
        SectionSeed("exploration", "exploration", "astronaut rocket launch", "rocket launch"),
        SectionSeed("phenomena", "phenomena", "aurora supernova comet", "aurora")
    )

    /**
     * Keeps the library current in the background: refreshes two collections per
     * run, rotating through the list by day so every section eventually updates.
     */
    suspend fun refreshLiveContent(): Int {
        val start = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % liveSections.size
        var added = 0
        repeat(2) { offset ->
            val seed = liveSections[(start + offset) % liveSections.size]
            val outcome = runCatching {
                loadPage(
                    queryKey = seed.key,
                    categoryKey = seed.category,
                    wikimediaQuery = seed.wikimediaQuery,
                    nasaQuery = seed.nasaQuery,
                    page = 1
                )
            }.getOrNull()
            added += outcome?.items?.size ?: 0
        }
        prefs.setLastSync(System.currentTimeMillis())
        return added
    }

    suspend fun pruneCache() {
        val cutoff = System.currentTimeMillis() - 45L * 24 * 60 * 60 * 1000
        dao.pruneOlderThan(cutoff)
    }

    fun dayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    companion object {
        const val PAGE_SIZE = 24
    }
}
