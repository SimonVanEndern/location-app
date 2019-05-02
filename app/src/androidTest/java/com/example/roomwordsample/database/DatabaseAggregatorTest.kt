package com.example.roomwordsample.database

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.roomwordsample.database.schemata.ActivityDao
import com.example.roomwordsample.database.schemata.ActivityTransition
import com.example.roomwordsample.database.schemata.ActivityTransitionDao
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DatabaseAggregatorTest : DatabaseTest() {

    private lateinit var activityDao: ActivityDao
    private lateinit var activityTransitionDao: ActivityTransitionDao

    @Before
    fun init() {
//        super.createDb()
        activityDao = getDb().activityDao()
        activityTransitionDao = getDb().activityTransitionDao()

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

        val id = activityTransitionDao.insert(activityTransition1)
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

}