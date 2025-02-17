package edu.gvsu.art.gallery.ui.favorites

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val favoritesModule = module {
    viewModel<FavoritesIndexViewModel> { FavoritesIndexViewModel(get(), get()) }
}
