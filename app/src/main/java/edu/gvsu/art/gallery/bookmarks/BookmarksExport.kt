package edu.gvsu.art.gallery.bookmarks

data class BookmarksExport(val html: String) {
    class Builder {
        private val openingText =
            """
            |$DOCTYPE
            |$METADATA
            |<TITLE>Favorites</TITLE>
            |<H1>Favorites</H1>
            |<DL><p>
            """.trimMargin()

        private val closingText =
            """
            |</DL><p>
            """.trimMargin()

        private var html = "\n"

        fun addBookmark(title: String, link: String): Builder {
            val parsedTitle = title.escapingSpecialXMLCharacters
            val parsedLink = link.escapingSpecialXMLCharacters

            html += "    <DT><A HREF=\"$parsedLink\">$parsedTitle</A>\n"

            return this
        }

        fun build(): BookmarksExport {
            val result = openingText + html + closingText

            return BookmarksExport(html = result)
        }
    }

    companion object {
        private const val DOCTYPE = "<!DOCTYPE NETSCAPE-Bookmark-file-1>"
        private const val METADATA =
            "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">"
        const val DEFAULT_FILE_NAME = "favorites.html"
    }
}

/**
 * Returns the string with the special XML characters (other than single-quote) ampersand-escaped.
 *
 * The four escaped characters are `<`, `>`, `&`, and `"`.
 */
val String.escapingSpecialXMLCharacters: String
    get() {
        var escaped = ""

        this.toCharArray().forEach { char ->
            when (char) {
                '&' -> escaped += "&amp;"
                '<' -> escaped += "&lt;"
                '>' -> escaped += "&gt;"
                '\"' -> escaped += "&quot;"
                else -> escaped += char
            }
        }

        return escaped
    }
