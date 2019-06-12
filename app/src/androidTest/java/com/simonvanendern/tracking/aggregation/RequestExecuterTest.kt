package com.simonvanendern.tracking.aggregation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.data_model.AggregationRequest
import com.simonvanendern.tracking.database.data_model.aggregated.ActivityDao
import com.simonvanendern.tracking.database.data_model.aggregated.StepsDao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class RequestExecuterTest {

    lateinit var db: TrackingDatabase
    lateinit var stepsDao: StepsDao
    lateinit var activityDao: ActivityDao

    private val averageSteps = 4321.12f
    private val timeInActivity1 = 1232342343L

    private var formatter = SimpleDateFormat("yyyy-MM-dd")

    private lateinit var executer: RequestExecuter


    @Before
    fun setUp() {
        stepsDao = mock(StepsDao::class.java)
        activityDao = mock(ActivityDao::class.java)
        db = mock(TrackingDatabase::class.java)

        executer = RequestExecuter(db)
    }

    @Test
    fun testStepsAggregation() {
        `when`(db.stepsDao())
            .thenReturn(stepsDao)

        `when`(stepsDao.getAverageSteps(any() ?: Date(), any() ?: Date()))
            .thenReturn(averageSteps)

        val request = AggregationRequest(
            1,
            "1",
            "user",
            Date(),
            Date(),
            "steps",
            3,
            2300f,
            mutableListOf(),
            true
        )

        val result = executer.execute(request)

        assertEquals(0, result.id)
        assertEquals(request.serverId, result.serverId)
        assertEquals(request.n + 1, result.n)
        assertEquals((request.value * request.n + averageSteps) / (request.n + 1), result.value)
        assertFalse(result.incoming)
    }

    @Test
    fun testAverageTimeInActivityAggregation() {
        val date1 = "2019-01-01"
        val date2 = "2019-01-05"
        val exactDifferenceInDays = 4

        `when`(db.activityDao())
            .thenReturn(activityDao)

        `when`(
            activityDao.getTotalTimeSpentOnActivity(
                any() ?: formatter.parse(date1),
                any() ?: formatter.parse(date2),
                ArgumentMatchers.eq(1)
            )
        ).thenReturn(timeInActivity1)

        val request = AggregationRequest(
            1,
            "1",
            "user",
            formatter.parse(date1),
            formatter.parse(date2),
            "activity_1",
            4,
            43214334345f,
            mutableListOf(),
            true
        )

        val result = executer.execute(request)

        assertEquals(0, result.id)
        assertEquals(request.serverId, result.serverId)
        assertEquals(request.n + 1, result.n)
        assertEquals(
            (request.value * request.n + timeInActivity1 / exactDifferenceInDays) / (request.n + 1),
            result.value
        )
        assertFalse(result.incoming)
    }
}