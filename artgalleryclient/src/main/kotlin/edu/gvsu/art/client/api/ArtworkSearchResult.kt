package edu.gvsu.art.client.api

import com.squareup.moshi.*

data class ArtworkSearchResult(
    var ok: Boolean? = false,
    var objectDetails: MutableList<FeaturedObjectDetail> = mutableListOf(),
)

class ArtworkSearchResultAdapter {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<FeaturedObjectDetail>,
    ): ArtworkSearchResult {
        val result = ArtworkSearchResult()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "ok") {
                result.ok = reader.nextBoolean()
            } else {
                result.objectDetails.add(delegate.fromJson(reader)!!)
            }
        }
        reader.endObject()
        return result
    }

    @ToJson
    fun toJson(
        writer: JsonWriter,
        result: ArtworkSearchResult,
        delegate: JsonAdapter<FeaturedObjectDetail>,
    ) {
        writer.beginObject()
        writer.name("ok")
        writer.value(result.ok)
        result.objectDetails.forEach { objectDetail ->
            writer.name(objectDetail.object_id!!.toString())
            delegate.toJson(writer, objectDetail)
        }
        writer.endObject()
    }
}
