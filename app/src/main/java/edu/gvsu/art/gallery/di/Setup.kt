package edu.gvsu.art.gallery.di

import edu.gvsu.art.gallery.ui.artwork.detail.artworkDetailModule
import org.koin.core.KoinApplication

fun KoinApplication.setupModules() {
    modules(
        platformModule,
        databaseModule,
        repositoryModule,
        artworkDetailModule,
    )
}
