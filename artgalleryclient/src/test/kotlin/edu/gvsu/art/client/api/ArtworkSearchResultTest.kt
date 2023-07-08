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
