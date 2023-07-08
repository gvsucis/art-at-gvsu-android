package edu.gvsu.art.client.common

import edu.gvsu.art.client.splitOnCommas
import java.net.URL

fun String?.asUrls(): List<URL> {
    return try {
        this?.splitOnCommas()?.filter { it.isNotBlank() }?.map { URL(it) } ?: listOf()
    } catch (e: NullPointerException) {
        listOf()
    }
}
