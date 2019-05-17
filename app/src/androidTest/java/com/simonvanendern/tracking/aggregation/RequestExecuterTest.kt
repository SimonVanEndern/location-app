package com.simonvanendern.tracking.aggregation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.AggregationRequest
import com.simonvanendern.tracking.database.schemata.StepsDao
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import java.util.*

@RunWith(AndroidJUnit4::class)
class RequestExecuterTest {

    lateinit var db: TrackingDatabase
    lateinit var stepsDao: StepsDao

    private val averageSteps = 4321.12f

    @Before
    fun setUp() {
        stepsDao = mock(StepsDao::class.java)
        db = mock(TrackingDatabase::class.java)

        `when`(db.stepsDao())
            .thenReturn(stepsDao)

        `when`(stepsDao.getAverageSteps(any() ?: Date(), any() ?: Date()))
            .thenReturn(averageSteps)
    }

    @Test
    fun testStepsAggregation() {
        val request = AggregationRequest(
            1, "1", "user", "steps", 3, 2300f, Date(), Date(), true
        )

        val executer = RequestExecuter(db)
        val result = executer.execute(request)

        assertEquals(request.id, result.id)
        assertEquals(request.n + 1, result.n)
        assertEquals((request.value * request.n + averageSteps) / (request.n + 1), result.value)
        assertEquals(false, result.incoming)
    }
}