package edu.gvsu.art.gallery.ui.browse

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val browseModule = module {
    viewModel<BrowseIndexViewModel> { BrowseIndexViewModel(get()) }
    viewModel<ArtworkCollectionViewModel> { ArtworkCollectionViewModel(get(), get()) }
}
