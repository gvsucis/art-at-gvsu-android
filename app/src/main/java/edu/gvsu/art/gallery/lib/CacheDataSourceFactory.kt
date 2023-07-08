package edu.gvsu.art.gallery.lib

import android.content.Context
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import edu.gvsu.art.gallery.BuildConfig

class CacheDataSourceFactory(
    private val context: Context,
    private val maxFileSize: Long,
) : DataSource.Factory {
    private val simpleCache: SimpleCache by lazy {
        VideoCache.getInstance(context)
    }

    private val defaultDatasourceFactory: DefaultDataSource.Factory
    override fun createDataSource(): DataSource {
        return CacheDataSource(
            simpleCache, defaultDatasourceFactory.createDataSource(),
            FileDataSource(), CacheDataSink(simpleCache, maxFileSize),
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
        )
    }

    init {
        val userAgent = Util.getUserAgent(
            context,
            BuildConfig.APPLICATION_NAME
        )
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        defaultDatasourceFactory = DefaultDataSource.Factory(
            this.context,
            DefaultHttpDataSource.Factory()
                .setUserAgent(userAgent)
                .setTransferListener(bandwidthMeter)
        ).setTransferListener(bandwidthMeter)
    }
}
