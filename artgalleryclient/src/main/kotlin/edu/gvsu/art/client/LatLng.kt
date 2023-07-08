package edu.gvsu.art.client

data class LatLng(val latitude: Double, val longitude: Double) {
    companion object {
        fun fromCoordinates(latitude: Double?, longitude: Double?): LatLng? {
            if (latitude == null || longitude == null) {
                return null
            }
            return LatLng(latitude, longitude)
        }
    }
}
