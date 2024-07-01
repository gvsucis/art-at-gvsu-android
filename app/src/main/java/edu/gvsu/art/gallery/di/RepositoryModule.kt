package edu.gvsu.art.gallery.di

import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.repository.*
import edu.gvsu.art.gallery.BuildConfig
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

internal val repositoryModule = module {
    single {
        ArtGalleryClient.create(
            baseURL = BuildConfig.ART_GALLERY_BASE_URL,
            cacheDirectory = androidApplication().cacheDir
        )
    }
    single<ArtistRepository> { DefaultArtistRepository(database = get(), client = get()) }
    single<ArtworkRepository> { DefaultArtworkRepository(database = get(), client = get()) }
    single<CampusRepository> { DefaultCampusRepository(database = get(), client = get()) }
    single<FavoritesRepository> { DefaultFavoritesRepository(database = get()) }
    single<ArtworkSearchRepository> { DefaultArtworkSearchRepository(client = get()) }
    single<LocationRepository> { DefaultLocationRepository(client = get()) }
    single<TourRepository> { DefaultTourRepository(client = get()) }
}
