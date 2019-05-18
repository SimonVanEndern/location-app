package com.simonvanendern.tracking.aggregation

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.database.schemata.*
import com.simonvanendern.tracking.repository.ActivityRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class DatabaseAggregatorTest : DatabaseTest() {

    private lateinit var activityDao: ActivityDao
    private lateinit var activityTransitionDao: ActivityTransitionDao
    private lateinit var stepsDao: StepsDao
    private lateinit var stepsRawDao: StepsRawDao
    private lateinit var activityRepository: ActivityRepository

    private var formatter = SimpleDateFormat("yyyy-MM-dd")

    @Before
    fun init() {
        activityDao = getDb().activityDao()
        activityTransitionDao = getDb().activityTransitionDao()
        activityRepository = ActivityRepository(activityTransitionDao, activityDao)
        stepsDao = getDb().stepsDao()
        stepsRawDao = getDb().stepsRawDao()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            // Set log level to Log.DEBUG to make it easier to debug
            .setMinimumLoggingLevel(Log.DEBUG)
            // Use a SynchronousExecutor here to make it easier to write tests
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testComputeOneActivity() {
        val activityTransition1 = ActivityTransition(0, Date(), 5, 0, Date().time + 100, false)
        val activityTransition2 = ActivityTransition(0, Date(), 5, 1, Date().time + 200, false)

        Log.d("TEST", activityTransitionDao.getAll().size.toString())

        activityTransitionDao.insert(activityTransition1)
        val id2 = activityTransitionDao.insert(activityTransition2)

        assertEquals(2, id2.toInt())

        var activityTransitions = activityTransitionDao.getAll()

        assertEquals(2, activityTransitions.size)

        DatabaseAggregator.aggregateActivities(activityRepository)
//        val request = OneTimeWorkRequest.Builder(DatabaseAggregator::class.java)
//            .build()
//
//        WorkManager.getInstance().enqueue(request).result.get()

        val activities = activityDao.getAll()
        activityTransitions = activityTransitionDao.getAll()

        assertEquals(1, activities.size)
        assertEquals(100, activities.first().duration)
        assertEquals(true, activityTransitions.map(ActivityTransition::processed)
            .reduce { acc, b -> acc && b })
    }

    @Test
    fun testComputeOneSteps() {
        val day1 = "2019-01-02"

        val stepsRaw = StepsRaw(
            formatter.parse(day1).time,
            formatter.parse(day1),
            50,
            false
        )

        stepsRawDao.insert(stepsRaw)

        val request = OneTimeWorkRequest.Builder(DatabaseAggregator::class.java).build()
        WorkManager.getInstance().enqueue(request).result.get()

        val stepsRawResult = stepsRawDao.getAll()
        assertEquals(1, stepsRawResult.size)
        assertEquals(true, stepsRawResult.first().processed)

        val averageSteps = stepsDao.getAverageSteps(formatter.parse(day1), formatter.parse(day1))
        assertEquals(50, averageSteps.toInt())
    }

    @Test
    fun testComputeSteps() {
        val day1 = "2019-01-02"
        val day2 = "2019-01-03"
        val day3 = "2019-01-04"

        val stepsRaw = arrayOf(
            StepsRaw(formatter.parse(day1).time, formatter.parse(day1), 50, false),
            StepsRaw(formatter.parse(day1).time + 10000, formatter.parse(day1), 200, false),
            StepsRaw(formatter.parse(day1).time + 30000, formatter.parse(day1), 20, false),
            StepsRaw(formatter.parse(day2).time + 200, formatter.parse(day2), 40, false),
            StepsRaw(formatter.parse(day3).time + 100, formatter.parse(day3), 10, false)
        )

        var steps = stepsDao.getAll()
        assertEquals(0, steps.size)

        stepsRaw.forEach { steps -> stepsRawDao.insert(steps) }
        var stepsRawResult = stepsRawDao.getAll()
        assertEquals(5, stepsRawResult.size)

        val request = OneTimeWorkRequest.Builder(DatabaseAggregator::class.java)
            .build()
        WorkManager.getInstance().enqueue(request).result.get()

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