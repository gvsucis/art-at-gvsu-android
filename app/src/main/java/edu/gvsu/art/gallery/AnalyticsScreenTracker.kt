package edu.gvsu.art.gallery

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

fun FirebaseAnalytics.logScreen(screenName: String) {
    val bundle = Bundle()
    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
    logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
}
