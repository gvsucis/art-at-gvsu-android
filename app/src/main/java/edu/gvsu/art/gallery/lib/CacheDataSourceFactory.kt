package edu.gvsu.art.gallery.lib

import android.content.Context
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import edu.gvsu.art.gallery.BuildConfig

@androidx.media3.common.util.UnstableApi
class CacheDataSourceFactory(
    private val context: Context,
    private val maxFileSize: Long,
) : DataSource.Factory {
    private val simpleCache: SimpleCache = VideoCache.getInstance(context)

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
