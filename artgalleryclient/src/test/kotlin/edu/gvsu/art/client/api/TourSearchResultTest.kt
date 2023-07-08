package edu.gvsu.art.client.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.test.*

internal class TourSearchResultTest {
    lateinit var jsonAdapter: JsonAdapter<TourSearchResult>

    @BeforeTest
    fun before() {
        val moshi: Moshi = Moshi.Builder()
            .add(TourSearchResultAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
        jsonAdapter = moshi.adapter(TourSearchResult::class.java)
    }

    @Test
    fun test_fromJson() {
        val json = """
            {
                "ok": true,
                "3": {
                    "access": "1",
                    "tour_id": 3,
                    "tour_name": "GVSU Favorites (Pew Campus)",
                    "tour_description": "Fifty artworks were selected on by the GVSU community"
                }
            }
        """.trimIndent()

        val expectedResult = TourSearchResult(
            ok = true,
            tourSearchDetails = mutableListOf(
                TourSearchDetail(
                    access = "1",
                    tour_id = 3,
                    tour_name = "GVSU Favorites (Pew Campus)",
                    tour_description = "Fifty artworks were selected on by the GVSU community"
                )
            )
        )
        val result = jsonAdapter.fromJson(json)

        assertEquals(expected = expectedResult, actual = result)
    }

    @Test
    fun test_toJson() {
        val tourSearchResult = TourSearchResult(
            ok = true,
            tourSearchDetails = mutableListOf(
                TourSearchDetail(
                    access = "1",
                    tour_id = 3,
                    tour_name = "GVSU Favorites (Pew Campus)",
                    tour_description = "Fifty artworks were selected on by the GVSU community"
                )
            )
        )
        val expectedJsonString = """
            {"ok":true,"3":{"access":"1","tour_id":3,"tour_name":"GVSU Favorites (Pew Campus)","tour_description":"Fifty artworks were selected on by the GVSU community"
        """.trimIndent()
        val json = jsonAdapter.toJson(tourSearchResult)

        assertContains(json, expectedJsonString)
    }
}
