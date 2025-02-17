package edu.gvsu.art.gallery.bookmarks

import org.koin.dsl.module

val bookmarksModule = module {
    single<BookmarksImporter> { BookmarksImporter(get(), get()) }
}
