package edu.gvsu.art.gallery.notifications

import android.os.Build

enum class Notifications(val channelID: String) {
    BOOKMARKS_IMPORT(channelID = "bookmarks_import");

    companion object {
        val askForPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        const val BOOKMARKS_IMPORT_NOTIFICATION_ID = 5_170_000
    }
}
