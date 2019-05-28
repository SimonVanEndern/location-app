package com.simonvanendern.tracking.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

@RunWith(AndroidJUnit4::class)
class WebServiceTest {

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
        val pw = "testPw"
        val response = "{\"userId\":\"$userId\",\"pw\":\"$pw\"}"

        server.enqueue(MockResponse().setBody(response))
        server.start()

        val url = server.url(testUrl)

        val webservice = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WebService::class.java)

        val result = webservice.createUser(User(userId, "")).execute()
        assertTrue(result.isSuccessful)

        assertEquals(pw, result.body()?.pw)
    }

    @Test
    fun testGetRequestsForUser() {
        val date1 = Date()
        val date2 = Date()
        val request1 = AggregationRequest("22", userId, "steps", 1, 1.0f, date1, date2)
        val request2 = AggregationRequest("23", userId, "steps", 1, 1.1f, date1, date2)

        val gson = Gson()
        val response = gson.toJson(arrayOf(request1, request2))

        server.enqueue(MockResponse().setBody(response))
        server.start()

        val url = server.url(testUrl)

        val webservice = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WebService::class.java)

        val result: List<AggregationRequest>? = webservice.getRequestsForUser(userId, "testPw").execute().body()

        assertTrue(result?.map(AggregationRequest::serverId)?.contains(request1.serverId) ?: false)
        assertTrue(result?.map(AggregationRequest::serverId)?.contains(request2.serverId) ?: false)
        assertTrue(result?.map(AggregationRequest::nextUser)?.contains(request1.nextUser) ?: false)
        assertTrue(result?.map(AggregationRequest::nextUser)?.contains(request2.nextUser) ?: false)
        assertEquals(result?.first()?.value, request1.value)
        assertEquals(result?.last()?.value, request2.value)
    }

    @Test
    fun testForwardAggregationRequest() {
        val request1 = AggregationRequest("22", userId, "steps", 1, 1.0f, Date(), Date())

        val response = "{\"status\":true}"

        server.enqueue(MockResponse().setBody(response))
        server.start()

        val url = server.url(testUrl)

        val webservice = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WebService::class.java)

        val result: Response? = webservice.forwardAggregationRequest(request1).execute().body()

        assertEquals(true, result?.status)
    }

    @Ignore
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
            .create(WebService::class.java)

//        val response: Response? = webservice.insertAggregationResult(result).execute().body()

//        assertEquals(true, response?.status)
    }
}