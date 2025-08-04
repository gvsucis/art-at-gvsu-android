package edu.gvsu.art.client.common

import java.net.URL
import java.util.regex.Pattern

object MediaTypes {
    fun isVideo(url: URL): Boolean {
        val extension = getFileExtensionFromUrl(url.toString())
        return knownVideoExtensions.contains(extension)
    }

    /**
     * Returns the file extension or an empty string if there is no
     * extension. This method is a convenience method for obtaining the
     * extension of a url and has undefined results for other Strings.
     * @param url
     * @return The file extension of the given url.
     *
     * * [Source](https://android.googlesource.com/platform/frameworks/base/+/61ae88e/core/java/android/webkit/MimeTypeMap.java#57)
     */
    fun getFileExtensionFromUrl(url: String?): String {
        var url = url
        if (!url.isNullOrEmpty()) {
            val fragment = url.lastIndexOf('#')
            if (fragment > 0) {
                url = url.substring(0, fragment)
            }

            val query = url.lastIndexOf('?')
            if (query > 0) {
                url = url.substring(0, query)
            }

            val filenamePos = url.lastIndexOf('/')
            val filename: String =
                (if (0 <= filenamePos) url.substring(filenamePos + 1) else url)

            // if the filename contains special characters, we don't
            // consider it valid for our matching purposes:
            if (!filename.isEmpty() &&
                Pattern.matches("[a-zA-Z_0-9.\\-()%]+", filename)
            ) {
                val dotPos = filename.lastIndexOf('.')
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1)
                }
            }
        }

        return ""
    }

    private val knownVideoExtensions = listOf(
        "m4v",
        "mp4",
    )
}