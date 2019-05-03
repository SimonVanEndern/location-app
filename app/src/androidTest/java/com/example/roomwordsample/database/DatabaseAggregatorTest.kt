package com.example.roomwordsample.database

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.roomwordsample.database.schemata.*
import org.junit.Assert
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

    private var formatter = SimpleDateFormat("yyyy-MM-dd")

    @Before
    fun init() {
//        super.createDb()
        activityDao = getDb().activityDao()
        activityTransitionDao = getDb().activityTransitionDao()
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

        Assert.assertEquals(2, id2.toInt())

        var activityTransitions = activityTransitionDao.getAll()

        Assert.assertEquals(2, activityTransitions.size)

        val request = OneTimeWorkRequest.Builder(DatabaseAggregator::class.java)
            .build()
        WorkManager.getInstance().enqueue(request).result.get()

        val activities = activityDao.getAll()
        activityTransitions = activityTransitionDao.getAll()

        Assert.assertEquals(true, activityTransitions.map(ActivityTransition::processed)
            .reduce { acc, b -> acc && b })
        Assert.assertEquals(1, activities.size)
        Assert.assertEquals(100, activities.first().duration)
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
        Assert.assertEquals(0, steps.size)

        stepsRaw.forEach { steps -> stepsRawDao.insert(steps) }
        var stepsRawResult = stepsRawDao.getAll()
        Assert.assertEquals(5, stepsRawResult.size)

        val request = OneTimeWorkRequest.Builder(DatabaseAggregator::class.java)
            .build()
        WorkManager.getInstance().enqueue(request).result.get()

        stepsRawResult = stepsRawDao.getAll()
        Assert.assertEquals(5, stepsRawResult.size)


        Assert.assertEquals(true, stepsRawResult.map(StepsRaw::processed)
            .reduce { acc, s -> acc && s })
        steps = stepsDao.getAll()
        Assert.assertEquals(4, steps.size)


        val totalSteps1 = stepsDao.getTotalStepsByDay(formatter.parse(day1))
        val totalSteps2 = stepsDao.getTotalStepsByDay(formatter.parse(day2))
        val totalSteps3 = stepsDao.getTotalStepsByDay(formatter.parse(day3))

        Assert.assertEquals(190, totalSteps1)
        Assert.assertEquals(10, totalSteps2)
        Assert.assertEquals(0, totalSteps3)

        val averageSteps = stepsDao.getAverageSteps(formatter.parse(day1), formatter.parse(day3))

        // The third day has no entry and thus is not counted in this metric
        Assert.assertEquals(100, averageSteps.toInt())
    }

}