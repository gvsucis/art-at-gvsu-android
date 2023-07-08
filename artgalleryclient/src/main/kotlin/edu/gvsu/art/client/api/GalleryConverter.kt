package edu.gvsu.art.client.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object GalleryConverter {
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(ArtworkSearchResultAdapter())
            .add(CampusSearchResultAdapter())
            .add(TourSearchResultAdapter())
            .add(ArtistSearchResultAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }
}
