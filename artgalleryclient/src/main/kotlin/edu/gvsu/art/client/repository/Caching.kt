package edu.gvsu.art.client.repository

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun isFreshCache(cacheDateTime: String?): Boolean {
    val today = LocalDateTime.now(ZoneId.of("UTC"))
    val cacheTime =
        LocalDateTime.parse(cacheDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    // cache time occurred less than a day ago
    return cacheTime < today.plusDays(1)
}
