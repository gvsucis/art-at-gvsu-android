package edu.gvsu.art.gallery.lib

import kotlin.test.Test
import kotlin.test.assertEquals

class BookmarksExportTest {
    @Test
    fun `generates a Netscape bookmarks document`() {
        val favorites = listOf(
            Favorite(
                title = "GVSU Marching Band",
                link = "https://artgallery.gvsu.edu/Detail/objects/3881"
            ),
            Favorite(
                title = "I'm Halfway There...",
                link = "https://artgallery.gvsu.edu/Detail/objects/6836"
            ),
            Favorite(
                title = "Pulling Out The Boat",
                link = "https://artgallery.gvsu.edu/Detail/objects/9509"
            ),
        )

        val (html) = BookmarksExport.Builder()
            .apply {
                favorites.forEach { favorite ->
                    addBookmark(favorite.title, favorite.link)
                }
            }
            .build()

        val expected = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
            <TITLE>Favorites</TITLE>
            <H1>Favorites</H1>
            <DL><p>
                <DT><A HREF="https://artgallery.gvsu.edu/Detail/objects/3881">GVSU Marching Band</A>
                <DT><A HREF="https://artgallery.gvsu.edu/Detail/objects/6836">I'm Halfway There...</A>
                <DT><A HREF="https://artgallery.gvsu.edu/Detail/objects/9509">Pulling Out The Boat</A>
            </DL><p>
        """.trimIndent()

        assertEquals(expected = expected, actual = html)
    }

    data class Favorite(val title: String, val link: String)
}
