package edu.gvsu.art.client

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LatLng(val latitude: Double, val longitude: Double): Parcelable {
    companion object {
        fun fromCoordinates(latitude: Double?, longitude: Double?): LatLng? {
            if (latitude == null || longitude == null) {
                return null
            }
            return LatLng(latitude, longitude)
        }
    }
}
