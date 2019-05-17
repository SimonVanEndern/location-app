package com.simonvanendern.tracking.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.communication.Response
import com.simonvanendern.tracking.communication.User
import com.simonvanendern.tracking.communication.WebService
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.database.schemata.AggregationRequestDao
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.*
import retrofit2.Call

@RunWith(AndroidJUnit4::class)
class RequestRepositoryTest : DatabaseTest() {

    lateinit var webService: WebService
    lateinit var call: Call<Response>
    private val responseHolder: retrofit2.Response<Response> = retrofit2.Response.success(Response(true))

    private val user = "test"

    @Mock
    lateinit var aggregationRequestDao: AggregationRequestDao

    lateinit var requestRepository: RequestRepository

    inline fun <reified T: Any> mock() = mock(T::class.java)

    private fun any() = any(User::class.java) ?: User("")

    @Before
    fun setUp() {
        webService = mock(WebService::class.java)
        val call : Call<Response> = mock()

        initMocks(this)

        `when`(call.execute())
            .thenReturn(responseHolder)

        `when`(webService.createUser(any()))
            .thenReturn(call)

        requestRepository = RequestRepository(webService, aggregationRequestDao)
    }

    @Test
    fun testCreateUser() {
        val result = requestRepository.createUser(user)
        assertTrue(result)

        verify(webService).createUser(any())
    }
}