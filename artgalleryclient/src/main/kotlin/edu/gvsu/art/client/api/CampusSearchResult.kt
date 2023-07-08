package edu.gvsu.art.client.api

import com.squareup.moshi.*

data class CampusSearchResult(
    var ok: Boolean? = false,
    var locationSearchDetails: MutableList<LocationSearchDetail> = mutableListOf(),
)

class CampusSearchResultAdapter {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<LocationSearchDetail>,
    ): CampusSearchResult {
        val result = CampusSearchResult()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "ok") {
                result.ok = reader.nextBoolean()
            } else {
                result.locationSearchDetails.add(delegate.fromJson(reader)!!)
            }
        }
        reader.endObject()
        return result
    }

    @ToJson
    fun toJson(
        writer: JsonWriter,
        result: CampusSearchResult,
        delegate: JsonAdapter<LocationSearchDetail>,
    ) {
        writer.beginObject()
        writer.name("ok")
        writer.value(result.ok)
        result.locationSearchDetails.forEach { locationDetail ->
            writer.name(locationDetail.location_id!!.toString())
            delegate.toJson(writer, locationDetail)
        }
        writer.endObject()
    }
}
