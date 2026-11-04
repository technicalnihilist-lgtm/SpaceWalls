package com.spacewalls.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.spacewalls.app.data.model.ImageSource
import com.spacewalls.app.data.model.RightsStatus
import com.spacewalls.app.data.model.Wallpaper

/**
 * The offline mirror of everything the app has ever seen, including its full
 * attribution block. Cached rows are what make the app usable with no network
 * and what the Credits screen reads from.
 */
@Entity(
    tableName = "wallpapers",
    indices = [
        Index(value = ["source", "remoteId"], unique = true),
        Index(value = ["queryKey"]),
        Index(value = ["categoryKey"]),
        Index(value = ["cachedAt"]),
        Index(value = ["isFavorite"])
    ]
)
data class WallpaperEntity(
    @PrimaryKey val id: String,
    val source: String,
    val remoteId: String,
    val title: String,
    val description: String,
    val creator: String,
    val credit: String,
    val licenseName: String,
    val licenseUrl: String,
    val usageTerms: String,
    val sourceName: String,
    val sourcePageUrl: String,
    val originalUrl: String,
    val previewUrl: String,
    val thumbUrl: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val dateCreated: String?,
    val modifications: String,
    val rightsStatus: String,
    val rightsNote: String?,
    val queryKey: String,
    val categoryKey: String,
    val cachedAt: Long,
    val isFavorite: Boolean = false,
    val localPath: String? = null,
    val lastViewedAt: Long? = null,
    val viewCount: Int = 0
)

fun Wallpaper.toEntity(
    queryKey: String,
    categoryKey: String,
    cachedAt: Long = System.currentTimeMillis(),
    existing: WallpaperEntity? = null
): WallpaperEntity = WallpaperEntity(
    id = id,
    source = source.key,
    remoteId = remoteId,
    title = title,
    description = description,
    creator = creator,
    credit = credit,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    usageTerms = usageTerms,
    sourceName = sourceName,
    sourcePageUrl = sourcePageUrl,
    originalUrl = originalUrl,
    previewUrl = previewUrl,
    thumbUrl = thumbUrl,
    width = width,
    height = height,
    mimeType = mimeType,
    dateCreated = dateCreated,
    modifications = modifications,
    rightsStatus = rightsStatus.key,
    rightsNote = rightsNote,
    queryKey = if (queryKey.isNotBlank()) queryKey else existing?.queryKey.orEmpty(),
    categoryKey = if (categoryKey.isNotBlank()) categoryKey else existing?.categoryKey.orEmpty(),
    cachedAt = existing?.cachedAt ?: cachedAt,
    isFavorite = existing?.isFavorite ?: isFavorite,
    localPath = existing?.localPath ?: localPath,
    lastViewedAt = existing?.lastViewedAt,
    viewCount = existing?.viewCount ?: 0
)

fun WallpaperEntity.toWallpaper(): Wallpaper = Wallpaper(
    id = id,
    source = ImageSource.fromKey(source),
    remoteId = remoteId,
    title = title,
    description = description,
    creator = creator,
    credit = credit,
    licenseName = licenseName,
    licenseUrl = licenseUrl,
    usageTerms = usageTerms,
    sourceName = sourceName,
    sourcePageUrl = sourcePageUrl,
    originalUrl = originalUrl,
    previewUrl = previewUrl,
    thumbUrl = thumbUrl,
    width = width,
    height = height,
    mimeType = mimeType,
    dateCreated = dateCreated,
    modifications = modifications,
    rightsStatus = RightsStatus.fromKey(rightsStatus),
    rightsNote = rightsNote,
    collectionId = categoryKey,
    isFavorite = isFavorite,
    localPath = localPath
)
