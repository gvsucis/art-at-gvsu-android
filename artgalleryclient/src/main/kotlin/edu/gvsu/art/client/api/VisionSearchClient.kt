package edu.gvsu.art.client.api

import edu.gvsu.art.client.api.GalleryConverter.moshi
import edu.gvsu.art.client.api.visionsearch.SearchResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VisionSearchClient {
    @Multipart
    @POST("search")
    suspend fun search(@Part image: MultipartBody.Part): SearchResponse

    companion object {
        fun create(baseURL: String): VisionSearchClient {
            return Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(OkHttpClient())
                .build()
                .create()
        }
    }
}
