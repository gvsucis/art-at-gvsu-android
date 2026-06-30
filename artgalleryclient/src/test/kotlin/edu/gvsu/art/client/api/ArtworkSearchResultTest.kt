package edu.gvsu.art.client.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.test.*

internal class ArtworkSearchResultTest {
    lateinit var jsonAdapter: JsonAdapter<ArtworkSearchResult>

    @BeforeTest
    fun before() {
        val moshi: Moshi = Moshi.Builder()
            .add(ArtworkSearchResultAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
        jsonAdapter = moshi.adapter(ArtworkSearchResult::class.java)
    }

    @Test
    fun test_fromJson() {
        val json = """
            {
                "ok": true,
                "255": {
                    "access": "1",
                    "object_id": 255,
                    "idno": "2002.168.1a-c"
                }
            }
        """.trimIndent()

        val expectedResult = ArtworkSearchResult(
            ok = true,
            objectDetails = mutableListOf(
                FeaturedObjectDetail(
                    access = "1",
                    object_id = 255,
                    idno = "2002.168.1a-c"
                )
            )
        )
        val result = jsonAdapter.fromJson(json)

        assertEquals(expected = expectedResult, actual = result)
    }

    @Test
    fun test_fromJson_parsesARFields() {
        // Shaped after the live objectSearch?q=featured_ar response: AR video +
        // GLB model URLs ride along on the search payload (no detail fetch).
        val json = """
            {
                "ok": true,
                "2329": {
                    "access": "1",
                    "object_id": 2329,
                    "object_name": "Calavera de Don Quijote",
                    "media_medium_url": "https://artgallery.gvsu.edu/admin/media/medium.jpg",
                    "ar_digital_asset": "https://artgallery.gvsu.edu/admin/media/video.mp4",
                    "ar_3d_file": "https://artgallery.gvsu.edu/admin/media/model.glb"
                }
            }
        """.trimIndent()

        val detail = jsonAdapter.fromJson(json)!!.objectDetails.single()

        assertEquals("https://artgallery.gvsu.edu/admin/media/video.mp4", detail.ar_digital_asset)
        assertEquals("https://artgallery.gvsu.edu/admin/media/model.glb", detail.ar_3d_file)
    }

    @Test
    fun test_toJson() {
        val featuredArtResult = ArtworkSearchResult(
            ok = true,
            objectDetails = mutableListOf(
                FeaturedObjectDetail(
                    access = "1",
                    object_id = 255,
                    idno = "2002.168.1a-c"
                )
            )
        )
        val expectedJsonSubString = """
            "ok":true,"255":{"access":"1","object_id":255,"idno":"2002.168.1a-c"
        """.trimIndent()
        val json = jsonAdapter.toJson(featuredArtResult)

        assertContains(json, expectedJsonSubString)
    }
}
