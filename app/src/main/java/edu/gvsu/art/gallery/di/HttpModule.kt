package edu.gvsu.art.gallery.di

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.TimeUnit

private const val CACHE_SIZE_BYTES: Long = 10L * 1024 * 1024
private val FORCED_MAX_AGE_SECONDS: Long = TimeUnit.MINUTES.toSeconds(5)

internal val httpModule = module {
    single {
        val cacheDir = File(get<Context>().cacheDir, "http_cache")
        OkHttpClient.Builder()
            .cache(Cache(cacheDir, CACHE_SIZE_BYTES))
            .addNetworkInterceptor { chain ->
                chain.proceed(chain.request())
                    .newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, max-age=$FORCED_MAX_AGE_SECONDS")
                    .build()
            }
            .build()
    }
}
