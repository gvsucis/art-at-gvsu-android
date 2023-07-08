package edu.gvsu.art.client.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.test.*

internal class CampusSearchResultTest {
    lateinit var jsonAdapter: JsonAdapter<CampusSearchResult>

    @BeforeTest
    fun before() {
        val moshi: Moshi = Moshi.Builder()
            .add(CampusSearchResultAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
        jsonAdapter = moshi.adapter(CampusSearchResult::class.java)
    }

    @Test
    fun test_fromJson() {
        val json = """
            {
              "ok": true,
              "2": {
                "access": "1",
                "location_id": 2,
                "location_name": "Allendale Campus",
                "location_description": "1 Campus Dr, Allendale, MI 49401"
              }
            }
        """.trimIndent()

        val expectedResult = CampusSearchResult(
            ok = true,
            locationSearchDetails = mutableListOf(
                LocationSearchDetail(
                    access = "1",
                    location_id = 2,
                    location_name = "Allendale Campus",
                    location_description = "1 Campus Dr, Allendale, MI 49401"
                )
            )
        )
        val result = jsonAdapter.fromJson(json)

        assertEquals(expected = expectedResult, actual = result)
    }

    @Test
    fun test_toJson() {
        val campusSearchResult = CampusSearchResult(
            ok = true,
            locationSearchDetails = mutableListOf(
                LocationSearchDetail(
                    access = "1",
                    location_id = 2,
                    location_name = "Allendale Campus",
                    location_description = "1 Campus Dr, Allendale, MI 49401"
                )
            )
        )
        val expectedJsonString = """
            {"ok":true,"2":{"access":"1","location_id":2,"location_name":"Allendale Campus","location_description":"1 Campus Dr, Allendale, MI 49401"
        """.trimIndent()
        val json = jsonAdapter.toJson(campusSearchResult)

        assertContains(json, expectedJsonString)
    }
}
