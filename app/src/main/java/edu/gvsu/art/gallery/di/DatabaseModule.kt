package edu.gvsu.art.gallery.di

import com.squareup.sqldelight.android.AndroidSqliteDriver
import edu.gvsu.art.db.ArtGalleryDatabase
import org.koin.dsl.module

internal val databaseModule = module {
    single<ArtGalleryDatabase> {
        ArtGalleryDatabase(
            driver = AndroidSqliteDriver(
                schema = ArtGalleryDatabase.Schema,
                context = get(),
                name = "art_gallery.db"
            )
        )
    }
}
