package edu.gvsu.art.client.api

import com.squareup.moshi.*

data class TourSearchResult(
    var ok: Boolean? = false,
    var tourSearchDetails: MutableList<TourSearchDetail> = mutableListOf(),
)

class TourSearchResultAdapter {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<TourSearchDetail>,
    ): TourSearchResult {
        val result = TourSearchResult()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "ok") {
                result.ok = reader.nextBoolean()
            } else {
                result.tourSearchDetails.add(delegate.fromJson(reader)!!)
            }
        }
        reader.endObject()
        return result
    }

    @ToJson
    fun toJson(
        writer: JsonWriter,
        result: TourSearchResult,
        delegate: JsonAdapter<TourSearchDetail>,
    ) {
        writer.beginObject()
        writer.name("ok")
        writer.value(result.ok)
        result.tourSearchDetails.forEach { tourDetail ->
            writer.name(tourDetail.tour_id!!.toString())
            delegate.toJson(writer, tourDetail)
        }
        writer.endObject()
    }
}
