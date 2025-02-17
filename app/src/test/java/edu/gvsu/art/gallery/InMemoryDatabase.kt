package edu.gvsu.art.gallery

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import edu.gvsu.art.db.ArtGalleryDatabase

object InMemoryDatabase {
    operator fun invoke(): ArtGalleryDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ArtGalleryDatabase.Schema.create(driver)
        return ArtGalleryDatabase(driver)
    }
}
