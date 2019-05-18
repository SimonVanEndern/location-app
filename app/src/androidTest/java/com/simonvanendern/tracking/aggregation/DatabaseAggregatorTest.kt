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
import org.junit.Ignore
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

        activityRepository.aggregateActivities()
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

    @Ignore("The step sensor return the total step count since last restart")
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
}