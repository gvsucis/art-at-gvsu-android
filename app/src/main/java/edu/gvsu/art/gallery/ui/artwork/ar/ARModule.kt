package edu.gvsu.art.gallery.ui.artwork.ar

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val arModule = module {
    viewModel<ARExperienceViewModel> { ARExperienceViewModel(androidApplication(), get()) }
}
