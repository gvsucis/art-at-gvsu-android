package edu.gvsu.art.gallery

import android.app.Application
import android.content.Context
import edu.gvsu.art.gallery.di.setupModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.*

class GalleryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        startKoin {
            androidContext(this@GalleryApplication)
            setupModules()
        }
    }

    companion object {
        // TODO add official PROPERTY_ID here
        // The following line should be changed to include the correct property id.
        var appContext: Context? = null
            private set
    }
}
