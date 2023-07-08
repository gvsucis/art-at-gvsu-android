package edu.gvsu.art.client.repository

import edu.gvsu.art.client.LatLng
import kotlin.math.absoluteValue

internal fun parseLocationGeoreference(locationGeoreference: String?): LatLng? {
    locationGeoreference ?: return null
    val compactGeoreference = locationGeoreference.replace("\\s".toRegex(), "")

    val coordinates = mutableListOf<Double>()
    val deque = ArrayDeque<Char>()
    compactGeoreference.forEachIndexed { index, token ->
        if (token == '-' || token == '.' || token.isDigit()) {
            deque.add(token)
        } else if (token == '[' || token == ']' || token == ',') {
            if (deque.isEmpty()) {
                return@forEachIndexed
            }
            dequeueIntoCoordinate(deque, coordinates)
        }
        if (index == compactGeoreference.lastIndex && deque.isNotEmpty()) {
            dequeueIntoCoordinate(deque, coordinates)
        }
    }


    if (coordinates.size < 2 || coordinates.size % 2 != 0) {
        return null
    }

    val latitude = coordinates[0]
    val longitude = coordinates[1]

    return try {
        LatLng(latitude, longitude)
    } catch (exception: NumberFormatException) {
        null
    }
}

private fun dequeueIntoCoordinate(deque: ArrayDeque<Char>, coordinates: MutableList<Double>) {
    var coordinate = ""
    while (deque.isNotEmpty()) {
        coordinate += deque.removeFirst()
    }

    try {
        val numericCoordinate = coordinate.toDouble()
        if (isValidCoordinate(numericCoordinate)) {
            coordinates.add(numericCoordinate)
        }
    } catch (_: java.lang.NumberFormatException) {
        return
    }
}

private fun isValidCoordinate(coordinate: Double) =
    (MIN_LONGITUDE..MAX_LONGITUDE).contains(coordinate)

// https://en.wikipedia.org/wiki/Geographic_coordinate_system
// Longitude can be represented as -90 to +90 while
// longitude is -180 to +180
const val MAX_LONGITUDE = 180f
const val MIN_LONGITUDE = -180f

