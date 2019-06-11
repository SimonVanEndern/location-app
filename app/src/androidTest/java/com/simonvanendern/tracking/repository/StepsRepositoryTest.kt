package com.simonvanendern.tracking.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.data_model.aggregated.StepsDao
import com.simonvanendern.tracking.database.data_model.raw.StepsRaw
import com.simonvanendern.tracking.database.data_model.raw.StepsRawDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class StepsRepositoryTest : DatabaseTest() {

    @Mock
    lateinit var stepsDao: StepsDao

    @Mock
    lateinit var stepsRawDao: StepsRawDao

    lateinit var stepsRepository: StepsRepository

    private var formatter = SimpleDateFormat("yyyy-MM-dd")


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val db = mock(TrackingDatabase::class.java)
        `when`(db.stepsDao())
            .thenReturn(stepsDao)
        `when`(db.stepsRawDao())
            .thenReturn(stepsRawDao)
        stepsRepository = StepsRepository(db)
    }

    @Test
    fun testGet10RecentSteps() {
        stepsRepository.recentSteps

        verify(stepsDao).get10RecentSteps()
    }

    @Test
    fun testInsertRawSteps() {
        val stepsRaw = StepsRaw(100, Date(), 32, false)

        runBlocking { stepsRepository.insert(stepsRaw) }

        verify(stepsRawDao).insert(stepsRaw)
    }

    @Test
    fun testAggregateSteps() {
        stepsRawDao = db.stepsRawDao()
        stepsDao = db.stepsDao()
        stepsRepository = StepsRepository(db)

        val day1 = "2019-01-02"
        val day2 = "2019-01-03"
        val day3 = "2019-01-04"

        val stepsRaw = arrayOf(
            StepsRaw(
                formatter.parse(day1).time,
                formatter.parse(day1),
                50,
                false
            ),
            StepsRaw(
                formatter.parse(day1).time + 10000,
                formatter.parse(day1),
                200,
                false
            ),
            StepsRaw(
                formatter.parse(day1).time + 30000,
                formatter.parse(day1),
                20,
                false
            ),
            StepsRaw(
                formatter.parse(day2).time + 200,
                formatter.parse(day2),
                40,
                false
            ),
            StepsRaw(
                formatter.parse(day3).time + 100,
                formatter.parse(day3),
                10,
                false
            )
        )

        var steps = stepsDao.getAll()
        assertEquals(0, steps.size)

        stepsRaw.forEach { steps -> stepsRawDao.insert(steps) }
        var stepsRawResult = stepsRawDao.getAll()
        assertEquals(5, stepsRawResult.size)

        stepsRepository.aggregateSteps()

        stepsRawResult = stepsRawDao.getAll()
        assertEquals(5, stepsRawResult.size)


        assertEquals(true, stepsRawResult.map(StepsRaw::processed)
            .reduce { acc, s -> acc && s })
        steps = stepsDao.getAll()
        assertEquals(4, steps.size)


        val totalSteps1 = stepsDao.getTotalStepsByDay(formatter.parse(day1))
        val totalSteps2 = stepsDao.getTotalStepsByDay(formatter.parse(day2))
        val totalSteps3 = stepsDao.getTotalStepsByDay(formatter.parse(day3))

        assertEquals(10, totalSteps2)
        assertEquals(0, totalSteps3)
        assertEquals(190, totalSteps1)

        val averageSteps = stepsDao.getAverageSteps(formatter.parse(day1), formatter.parse(day3))

        // The third day has no entry and thus is not counted in this metric
        assertEquals(100, averageSteps.toInt())
    }
}