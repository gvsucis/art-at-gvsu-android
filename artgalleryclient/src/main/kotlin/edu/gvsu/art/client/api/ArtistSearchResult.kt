package edu.gvsu.art.client.api

import com.squareup.moshi.*

data class ArtistSearchResult(
    var ok: Boolean? = false,
    var entityDetails: MutableList<EntityDetail> = mutableListOf(),
)

class ArtistSearchResultAdapter {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<EntityDetail>,
    ): ArtistSearchResult {
        val result = ArtistSearchResult()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "ok") {
                result.ok = reader.nextBoolean()
            } else {
                result.entityDetails.add(delegate.fromJson(reader)!!)
            }
        }
        reader.endObject()
        return result
    }

    @ToJson
    fun toJson(
        writer: JsonWriter,
        result: ArtistSearchResult,
        delegate: JsonAdapter<EntityDetail>,
    ) {
        writer.beginObject()
        writer.name("ok")
        writer.value(result.ok)
        result.entityDetails.forEach { objectDetail ->
            writer.name(objectDetail.entity_id!!.toString())
            delegate.toJson(writer, objectDetail)
        }
        writer.endObject()
    }
}
