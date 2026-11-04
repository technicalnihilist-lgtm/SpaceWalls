package com.spacewalls.app.data.remote

import android.content.Context
import com.spacewalls.app.data.remote.nasa.NasaApi
import com.spacewalls.app.data.remote.wikimedia.WikimediaApi
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Shared HTTP stack. A descriptive User-Agent is required by both Wikimedia and
 * NASA etiquette guidelines, and the disk cache keeps repeat browsing cheap.
 */
class HttpClients(context: Context) {

    private val userAgent =
        "SpaceWalls/1.0 (Android; wallpaper client; +https://commons.wikimedia.org/)"

    private val headerInterceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .header("Accept", "application/json, image/*")
                .build()
        )
    }

    /**
     * Catalogue requests must never be served from the HTTP cache: the whole
     * point of the app is that imagery stays live. Image bytes are still cached
     * aggressively by Coil.
     */
    private val freshnessInterceptor = Interceptor { chain ->
        val request = chain.request()
        val isCatalogueCall = request.url.host.endsWith("wikimedia.org") ||
            request.url.host.endsWith("nasa.gov")
        val jsonRequest = request.newBuilder()
            .header("Cache-Control", if (isCatalogueCall) "no-cache, max-age=0" else "max-age=86400")
            .build()
        chain.proceed(jsonRequest)
    }

    val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(freshnessInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (com.spacewalls.app.BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
        )
        .cache(Cache(File(context.cacheDir, "http_cache"), 64L * 1024 * 1024))
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val gson = com.google.gson.GsonBuilder().setLenient().create()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val wikimedia: WikimediaApi by lazy {
        retrofit("https://commons.wikimedia.org/").create(WikimediaApi::class.java)
    }

    val nasa: NasaApi by lazy {
        retrofit("https://images-api.nasa.gov/").create(NasaApi::class.java)
    }
}
