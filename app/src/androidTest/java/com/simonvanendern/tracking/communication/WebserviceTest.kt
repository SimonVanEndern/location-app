package com.simonvanendern.tracking.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class WebserviceTest {

    val testUrl = "/testServer/"
    val userId = "iijoij23jfdsoijf"

    lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testCreateUser() {
        val response = "{\"status\":true}"

        server.enqueue(MockResponse().setBody(response))
        server.start()

        val url = server.url(testUrl)

        val webservice = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Webservice::class.java)

        val result: Response? = webservice.createUser(User(userId)).execute().body()

        assertEquals(true, result?.status)
    }

    @Test
    fun testGetRequestsForUser() {
        val request1 = AggregationRequest("22", userId, "1111111".toByteArray())
        val request2 = AggregationRequest("23", userId, "00000".toByteArray())

        val gson = Gson()
        val response = gson.toJson(arrayOf(request1, request2))

        server.enqueue(MockResponse().setBody(response))
        server.start()

        val url = server.url(testUrl)

        val webservice = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Webservice::class.java)

        val result: List<AggregationRequest>? = webservice.getRequestsForUser(userId).execute().body()

        assertTrue(result?.map(AggregationRequest::id)?.contains(request1.id) ?: false)
        assertTrue(result?.map(AggregationRequest::id)?.contains(request2.id) ?: false)
        assertTrue(result?.map(AggregationRequest::user)?.contains(request1.user) ?: false)
        assertTrue(result?.map(AggregationRequest::user)?.contains(request2.user) ?: false)
        assertArrayEquals(result?.first()?.data, request1.data)
        assertArrayEquals(result?.last()?.data, request2.data)
    }

    @Test
    fun testForwardAggregationRequest() {
        val request1 = AggregationRequest("22", userId, "1111111".toByteArray())

        val response = "{\"status\":true}"

        server.enqueue(MockResponse().setBody(response))
        server.start()

        val url = server.url(testUrl)

        val webservice = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Webservice::class.java)

        val result: Response? = webservice.forwardAggregationRequest(request1).execute().body()

        assertEquals(true, result?.status)
    }

    @Test
    fun testInsertAggregationResult() {
        val result = AggregationResult("22", userId, "1111111".toByteArray())

        val serverResponse = "{\"status\":true}"

        server.enqueue(MockResponse().setBody(serverResponse))
        server.start()

        val url = server.url(testUrl)

        val webservice = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Webservice::class.java)

        val response: Response? = webservice.insertAggregationResult(result).execute().body()

        assertEquals(true, response?.status)
    }
}