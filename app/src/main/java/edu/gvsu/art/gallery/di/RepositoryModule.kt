package edu.gvsu.art.gallery.di

import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.VisionSearchClient
import edu.gvsu.art.client.repository.*
import edu.gvsu.art.gallery.BuildConfig
import org.koin.dsl.module

internal val repositoryModule = module {
    single {
        ArtGalleryClient.create(
            baseURL = BuildConfig.ART_GALLERY_BASE_URL,
            client = get()
        )
    }
    single {
        VisionSearchClient.create(BuildConfig.VISION_SEARCH_BASE_URL)
    }
    single<ArtistRepository> { DefaultArtistRepository(client = get()) }
    single<ArtworkRepository> { DefaultArtworkRepository(client = get()) }
    single<CampusRepository> { DefaultCampusRepository(client = get()) }
    single<FavoritesRepository> { DefaultFavoritesRepository(database = get()) }
    single<ArtworkSearchRepository> { DefaultArtworkSearchRepository(client = get()) }
    single<LocationRepository> { DefaultLocationRepository(client = get()) }
    single<TourRepository> { DefaultTourRepository(client = get()) }
}
