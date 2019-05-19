package com.simonvanendern.tracking.repository

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.database.schemata.aggregated.ActivityDao
import com.simonvanendern.tracking.database.schemata.raw.ActivityTransition
import com.simonvanendern.tracking.database.schemata.raw.ActivityTransitionDao
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class ActivityRepositoryTest : DatabaseTest() {

    private lateinit var activityDao: ActivityDao
    private lateinit var activityTransitionDao: ActivityTransitionDao
    private lateinit var activityRepository: ActivityRepository

    @Before
    fun init() {
        activityDao = db.activityDao()
        activityTransitionDao = db.activityTransitionDao()
        activityRepository = ActivityRepository(db)
    }

    @Test
    fun testComputeOneActivity() {
        val activityTransition1 = ActivityTransition(
            0,
            Date(),
            5,
            0,
            Date().time + 100,
            false
        )
        val activityTransition2 = ActivityTransition(
            0,
            Date(),
            5,
            1,
            Date().time + 200,
            false
        )

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
}