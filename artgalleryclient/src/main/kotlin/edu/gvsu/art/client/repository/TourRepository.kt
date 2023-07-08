package edu.gvsu.art.client.repository

import edu.gvsu.art.client.Artwork
import edu.gvsu.art.client.Tour
import edu.gvsu.art.client.TourStop
import edu.gvsu.art.client.api.ArtGalleryClient
import edu.gvsu.art.client.api.TourStopDetail
import edu.gvsu.art.client.common.request
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

interface TourRepository {
    suspend fun all(): Result<List<Tour>>
    suspend fun find(tourID: String): Result<Tour>
}

class DefaultTourRepository(val client: ArtGalleryClient) : TourRepository {
    override suspend fun all(): Result<List<Tour>> {
        return request { client.fetchTours() }.fold(
            onSuccess = { result ->
                val tours: List<Tour> = result.toDomainModel.sortedBy { it.id }
                Result.success(tours)
            },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun find(tourID: String): Result<Tour> {
        return request { client.fetchTour(tourID) }.fold(
            onSuccess = {
                val tour = it.toDomainModel
                val tourStops = findTourStops(tour)
                Result.success(tour.copy(stops = tourStops))
            },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun findTourStops(tour: Tour): List<TourStop> {
        val tourStops = coroutineScope {
            tour.stops
                .map { async { client.fetchTourStop(it.id) } }
                .awaitAll()
        }

        return tourStops
            .filter { !it.stop_objects_id.isNullOrBlank() }
            .map(TourStopDetail::toDomainModel)
    }
}
