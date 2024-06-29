package edu.gvsu.art.client.api

import edu.gvsu.art.client.api.GalleryConverter.moshi
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File

interface ArtGalleryClient {
    @GET("objectDetail")
    suspend fun fetchObjectDetail(@Query("id") id: String): ObjectDetail

    @GET("entityDetail")
    suspend fun fetchEntityDetail(@Query("id") id: String): EntityDetail

    @GET("entitySearch")
    suspend fun fetchArtistSearch(@Query("q") query: String): ArtistSearchResult

    @GET("objectSearch")
    suspend fun fetchArtworkSearch(@Query("q") query: String): ArtworkSearchResult

    @GET("locationcampusSearch?q=*")
    suspend fun fetchCampuses(): CampusSearchResult

    @GET("locationDetail")
    suspend fun fetchLocationDetail(@Query("id") id: String): LocationDetail

    @GET("tourSearch?q=*")
    suspend fun fetchTours(): TourSearchResult

    @GET("tourDetail")
    suspend fun fetchTour(@Query("id") id: String): TourDetail

    @GET("tourstopsDetail")
    suspend fun fetchTourStop(@Query("id") id: String): TourStopDetail

    companion object {
        fun create(baseURL: String, cacheDirectory: File): ArtGalleryClient {
            val httpClient = OkHttpClient.Builder()
                .cache(
                    Cache(
                        File(cacheDirectory, "http_cache"),
                        50L * 1024L * 1024L // 50 MiB
                    )
                )
                .build()

            return Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(httpClient)
                .build()
                .create()
        }
    }
}
