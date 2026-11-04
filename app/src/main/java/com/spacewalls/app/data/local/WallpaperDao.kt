package com.spacewalls.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {

    @Upsert
    suspend fun upsertAll(items: List<WallpaperEntity>)

    @Upsert
    suspend fun upsert(item: WallpaperEntity)

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): WallpaperEntity?

    @Query("SELECT * FROM wallpapers WHERE id IN (:ids)")
    suspend fun byIds(ids: List<String>): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<WallpaperEntity?>

    @Query(
        "SELECT * FROM wallpapers WHERE queryKey = :queryKey " +
            "ORDER BY cachedAt DESC, title ASC"
    )
    fun observeQuery(queryKey: String): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY title ASC")
    fun observeFavorites(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY title ASC")
    suspend fun favorites(): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers WHERE cachedAt >= :since ORDER BY cachedAt DESC LIMIT :limit")
    fun observeRecentlyAdded(since: Long, limit: Int): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE lastViewedAt IS NOT NULL ORDER BY lastViewedAt DESC LIMIT :limit")
    fun observeRecentlyViewed(limit: Int): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE localPath IS NOT NULL ORDER BY title ASC")
    fun observeDownloaded(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers ORDER BY title ASC")
    fun observeAll(): Flow<List<WallpaperEntity>>

    @Query(
        "SELECT * FROM wallpapers WHERE title LIKE '%' || :term || '%' " +
            "OR description LIKE '%' || :term || '%' OR creator LIKE '%' || :term || '%' " +
            "ORDER BY cachedAt DESC LIMIT :limit"
    )
    suspend fun searchLocal(term: String, limit: Int): List<WallpaperEntity>

    @Query("UPDATE wallpapers SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("UPDATE wallpapers SET localPath = :path WHERE id = :id")
    suspend fun setLocalPath(id: String, path: String?)

    @Query(
        "UPDATE wallpapers SET lastViewedAt = :timestamp, viewCount = viewCount + 1 " +
            "WHERE id = :id"
    )
    suspend fun markViewed(id: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM wallpapers")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM wallpapers WHERE isFavorite = 1")
    fun observeFavoriteCount(): Flow<Int>

    @Query("DELETE FROM wallpapers WHERE isFavorite = 0 AND localPath IS NULL AND cachedAt < :before")
    suspend fun pruneOlderThan(before: Long)
}
