package edu.gvsu.art.gallery.di

import android.content.Context
import android.net.ConnectivityManager
import org.koin.dsl.module

internal val platformModule = module {
    single { get<Context>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
}
