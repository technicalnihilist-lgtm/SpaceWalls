package com.spacewalls.app.data.model

/** Where a wallpaper came from. Every source carries its own attribution rules. */
enum class ImageSource(
    val key: String,
    val displayName: String,
    val homepage: String
) {
    WIKIMEDIA("wikimedia", "Wikimedia Commons", "https://commons.wikimedia.org/"),
    NASA("nasa", "NASA Image and Video Library", "https://images.nasa.gov/");

    companion object {
        fun fromKey(key: String?): ImageSource =
            entries.firstOrNull { it.key == key } ?: WIKIMEDIA
    }
}

/**
 * How confident we are about the usage rights of an image.
 * Only [VERIFIED] and [THIRD_PARTY_CREDIT] images are ever shown as wallpapers;
 * anything we could not verify is dropped before it reaches the UI.
 */
enum class RightsStatus(val key: String) {
    /** Licence explicitly stated and recognised, attribution data complete. */
    VERIFIED("verified"),

    /** NASA content with a credit line naming a third party — usable, but flagged. */
    THIRD_PARTY_CREDIT("third_party");

    companion object {
        fun fromKey(key: String?): RightsStatus =
            entries.firstOrNull { it.key == key } ?: VERIFIED
    }
}

enum class WallpaperTarget { HOME, LOCK, BOTH }

/**
 * A single wallpaper together with every attribution field the app is required
 * to display. Nothing here is ever guessed: a field is empty when the source
 * did not provide it, and an image without a recognised licence never becomes
 * a [Wallpaper] at all.
 */
data class Wallpaper(
    val id: String,
    val source: ImageSource,
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
    val rightsStatus: RightsStatus,
    val rightsNote: String?,
    val collectionId: String = "",
    val isFavorite: Boolean = false,
    val localPath: String? = null
) {
    val resolution: String
        get() = if (width > 0 && height > 0) "$width × $height" else "Unknown"

    val isPortrait: Boolean
        get() = height > width

    /** Shareable text used when the image itself cannot be attached. */
    fun shareText(): String = buildString {
        appendLine(title)
        appendLine("by $creator")
        appendLine("Source: $sourceName")
        appendLine(sourcePageUrl)
        append("Licence: $licenseName")
        if (licenseUrl.isNotBlank()) append(" — $licenseUrl")
    }
}
