package com.spacewalls.app.data.remote.wikimedia

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Wikimedia Commons action API — the documented public endpoint at
 * https://commons.wikimedia.org/w/api.php.
 *
 * A single call returns the file metadata *and* its licence block
 * (extmetadata), which is what makes Commons usable without guessing rights.
 */
interface WikimediaApi {

    @GET("w/api.php")
    suspend fun searchImages(
        @Query("gsrsearch") search: String,
        @Query("gsroffset") offset: Int,
        @Query("gsrlimit") limit: Int,
        @Query("gsrnamespace") namespace: Int = 6,
        @Query("gsrsort") sort: String = "relevance",
        @Query("generator") generator: String = "search",
        @Query("prop") prop: String = "imageinfo",
        @Query("iiprop") infoProps: String = "url|size|mime|extmetadata|user",
        @Query("iiurlwidth") thumbWidth: Int = 1000,
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("formatversion") formatVersion: Int = 2
    ): WikimediaSearchResponse
}

data class WikimediaSearchResponse(
    @SerializedName("query") val query: WikimediaQuery? = null,
    @SerializedName("continue") val continuation: WikimediaContinuation? = null,
    @SerializedName("error") val error: WikimediaError? = null
)

data class WikimediaQuery(
    @SerializedName("pages") val pages: List<WikimediaPage>? = null
)

data class WikimediaContinuation(
    @SerializedName("gsroffset") val offset: Int? = null,
    @SerializedName("continue") val token: String? = null
)

data class WikimediaError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("info") val info: String? = null
)

data class WikimediaPage(
    @SerializedName("pageid") val pageId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("imageinfo") val imageInfo: List<WikimediaImageInfo>? = null
)

data class WikimediaImageInfo(
    @SerializedName("url") val url: String? = null,
    @SerializedName("descriptionurl") val descriptionUrl: String? = null,
    @SerializedName("thumburl") val thumbUrl: String? = null,
    @SerializedName("thumbwidth") val thumbWidth: Int? = null,
    @SerializedName("thumbheight") val thumbHeight: Int? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("size") val size: Long? = null,
    @SerializedName("mime") val mime: String? = null,
    @SerializedName("user") val uploader: String? = null,
    @SerializedName("extmetadata") val extMetadata: Map<String, WikimediaMetaValue>? = null
)

data class WikimediaMetaValue(
    @SerializedName("value") val value: String? = null
)
