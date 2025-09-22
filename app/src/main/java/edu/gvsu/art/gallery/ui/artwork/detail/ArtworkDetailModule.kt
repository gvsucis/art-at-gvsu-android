package edu.gvsu.art.gallery.ui.artwork.detail

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val artworkDetailModule = module {
    viewModel<ArtworkDetailViewModel> { ArtworkDetailViewModel(get(), get(), get()) }
}
