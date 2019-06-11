package com.simonvanendern.tracking.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.communication.AggregationRequest
import com.simonvanendern.tracking.communication.User
import com.simonvanendern.tracking.communication.WebService
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.data_model.AggregationRequestDao
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks
import retrofit2.Call
import java.util.*

@RunWith(AndroidJUnit4::class)
class RequestRepositoryTest : DatabaseTest() {

    lateinit var webService: WebService

    private val user = "test"

    @Mock
    lateinit var aggregationRequestDao: AggregationRequestDao

    lateinit var requestRepository: RequestRepository

    inline fun <reified T : Any> mock() = mock(T::class.java)

    private fun any() = any(User::class.java) ?: User("", "")

    private fun any2() = any(com.simonvanendern.tracking.database.data_model.AggregationRequest::class.java)
        ?: com.simonvanendern.tracking.database.data_model.AggregationRequest(
            0,
            "",
            "",
            "",
            0,
            0f,
            Date(),
            Date(),
            true,
            mutableListOf()
        )

    @Before
    fun setUp() {
        webService = mock(WebService::class.java)
        initMocks(this)

        val db = mock(TrackingDatabase::class.java)
        `when`(db.aggregationRequestDao())
            .thenReturn(aggregationRequestDao)

        requestRepository = RequestRepository(db, webService)
    }

    @Test
    fun testCreateUser() {
        val pw = "testPw"
        val responseHolder = retrofit2.Response.success(User(user, pw))

        val call: Call<User> = mock()

        `when`(call.execute())
            .thenReturn(responseHolder)

        `when`(webService.createUser(any()))
            .thenReturn(call)

        val result = requestRepository.createUser(user)
        assertEquals(user, result?.pk)
        assertEquals(pw, result?.pw)

        verify(webService).createUser(any())
    }

    @Test
    fun testGetPendingRequests() {
        val request1 = AggregationRequest("1", "wo0ifwdji", "steps", 1, 1.1f, Date(), Date(), mutableListOf())
        val request2 = AggregationRequest("2", "owfijoj2d", "steps", 1, 1.1f, Date(), Date(), mutableListOf())

        val responseHolder = retrofit2.Response.success(listOf(request1, request2))
        val call: Call<List<AggregationRequest>> = mock()

        `when`(call.execute())
            .thenReturn(responseHolder)

        `when`(webService.getRequestsForUser(user, "testPw"))
            .thenReturn(call)

        requestRepository.getPendingRequests(user, "testPw")

        verify(aggregationRequestDao, times(2)).insert(any2())
        verify(aggregationRequestDao, times(1)).getAllIncomingAggregationRequests()
    }
}