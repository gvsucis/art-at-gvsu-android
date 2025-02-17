package edu.gvsu.art.gallery.di

import edu.gvsu.art.gallery.bookmarks.bookmarksModule
import edu.gvsu.art.gallery.ui.artwork.detail.artworkDetailModule
import edu.gvsu.art.gallery.ui.browse.browseModule
import edu.gvsu.art.gallery.ui.favorites.favoritesModule
import org.koin.core.KoinApplication

fun KoinApplication.setupModules() {
    modules(
        platformModule,
        databaseModule,
        repositoryModule,
        artworkDetailModule,
        browseModule,
        favoritesModule,
        bookmarksModule,
    )
}
