package edu.gvsu.art.client.repository

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Cache time occurred less than a day ago
 */
internal fun isFreshCache(cacheDateTime: String?): Boolean {
    if (cacheDateTime.isNullOrBlank()) {
        return false
    }

    val now = LocalDateTime.now(ZoneId.of("UTC"))
    val cacheTime =
        LocalDateTime.parse(cacheDateTime, CACHE_TIME_FORMAT)

    return cacheTime.plusDays(1) > now
}

internal val CACHE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
