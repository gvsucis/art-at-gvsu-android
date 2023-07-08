package edu.gvsu.art.gallery.di

import org.koin.core.KoinApplication

fun KoinApplication.setupModules() {
    modules(platformModule)
    modules(databaseModule)
    modules(repositoryModule)
}
