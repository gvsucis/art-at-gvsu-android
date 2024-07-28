package edu.gvsu.art.gallery.lib

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi object VideoCache {
    private var simpleCache: SimpleCache? = null
    private const val maxCacheSize: Long = 100 * 1024 * 1024L
    fun getInstance(context: Context): SimpleCache {
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        if (simpleCache == null) simpleCache =
            SimpleCache(File(context.cacheDir, "media"), evictor, StandaloneDatabaseProvider(context))
        return simpleCache as SimpleCache
    }
}
