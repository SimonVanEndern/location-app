package com.simonvanendern.tracking.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class WebserviceTest {

    @Test
    fun testCreateUser () {
        val response = "{\"status\":true}"
        val testUrl = "/testServer/"
        val userId = "iijoij23jfdsoijf"

        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(response))
        server.start()

        val url = server.url(testUrl)

        val webservice = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Webservice::class.java)

        val result :Response? = webservice.createUser(User(userId)).execute().body()

        Assert.assertEquals(true, result?.status)

        server.shutdown()
    }
}