package edu.gvsu.art.gallery.ui.search

import edu.gvsu.art.gallery.ui.get
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    viewModel<VisionSearchResultsViewModel> {
        VisionSearchResultsViewModel(
            client = get(),
            handle = get(),
            application = get(),
        )
    }
}
