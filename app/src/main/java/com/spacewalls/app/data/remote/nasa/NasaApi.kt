package com.spacewalls.app.data.remote.nasa

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * NASA Images and Video Library API — https://images-api.nasa.gov/.
 * The search endpoint returns collection metadata; the per-item `href` is an
 * asset manifest listing the actual files served by images-assets.nasa.gov.
 */
interface NasaApi {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("media_type") mediaType: String = "image",
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): NasaSearchResponse

    /** Resolves an item's asset manifest (list of real file URLs). */
    @GET
    suspend fun assets(@Url manifestUrl: String): List<String>
}

data class NasaSearchResponse(
    @SerializedName("collection") val collection: NasaCollection? = null
)

data class NasaCollection(
    @SerializedName("items") val items: List<NasaItem>? = null,
    @SerializedName("metadata") val metadata: NasaCollectionMetadata? = null,
    @SerializedName("href") val href: String? = null
)

data class NasaCollectionMetadata(
    @SerializedName("total_hits") val totalHits: Int? = null
)

data class NasaItem(
    @SerializedName("href") val manifestHref: String? = null,
    @SerializedName("data") val data: List<NasaItemData>? = null,
    @SerializedName("links") val links: List<NasaLink>? = null
)

data class NasaItemData(
    @SerializedName("nasa_id") val nasaId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("description_508") val descriptionAlt: String? = null,
    @SerializedName("center") val center: String? = null,
    @SerializedName("secondary_creator") val secondaryCreator: String? = null,
    @SerializedName("date_created") val dateCreated: String? = null,
    @SerializedName("keywords") val keywords: List<String>? = null,
    @SerializedName("media_type") val mediaType: String? = null,
    @SerializedName("photographer") val photographer: String? = null
)

data class NasaLink(
    @SerializedName("href") val href: String? = null,
    @SerializedName("rel") val rel: String? = null,
    @SerializedName("render") val render: String? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null
)
