package edu.gvsu.art.gallery.lib

import android.content.Context
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

object VideoCache {
    private var simpleCache: SimpleCache? = null
    private const val maxCacheSize: Long = 100 * 1024 * 1024L
    fun getInstance(context: Context): SimpleCache {
        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        if (simpleCache == null) simpleCache =
            SimpleCache(File(context.cacheDir, "media"), evictor, StandaloneDatabaseProvider(context))
        return simpleCache as SimpleCache
    }
}
