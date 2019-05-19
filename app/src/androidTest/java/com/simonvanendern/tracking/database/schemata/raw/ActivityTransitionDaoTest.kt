package com.simonvanendern.tracking.database.schemata.raw

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.location.DetectedActivity
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.waitForValue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class ActivityTransitionDaoTest : DatabaseTest() {

    var formatter = SimpleDateFormat("dd-MM-yyyy")

    private lateinit var activityTransitionDao: ActivityTransitionDao

    @Before
    fun init() {
        activityTransitionDao = getDb().activityTransitionDao()
    }

    @Test
    fun testInsertTrajectory() {
        val day = formatter.parse("01-01-2019")
        val start = day.time + 10000
        val day2 = formatter.parse("01-01-2019")
        val start2 = day.time + 40000L
        val activity1 = ActivityTransition(
            0,
            day,
            DetectedActivity.ON_FOOT,
            0,
            start,
            false
        )
        val activity2 = ActivityTransition(
            0,
            day2,
            DetectedActivity.ON_BICYCLE,
            0,
            start2,
            false
        )
        activityTransitionDao.insert(activity1)
        activityTransitionDao.insert(activity2)
        val activities = activityTransitionDao.getActivitiesByDay(day)
        assertEquals(2, activities.size)
    }

    @Test
    fun testGet10RecentActivityTransition() {
        val dayFraction = "-01-2019"

        for (i in 11..30) {
            activityTransitionDao.insert(
                ActivityTransition(
                    i.toLong(),
                    formatter.parse("" + i + dayFraction),
                    DetectedActivity.ON_BICYCLE,
                    0,
                    formatter.parse("" + i + dayFraction).time + 10000,
                    false
                )
            )
        }

        val result = activityTransitionDao.get10RecentActivityTransitions()
            .waitForValue()
        assertEquals(10, result.size)
        assertEquals(30, result.first().id)
    }

    @Test
    fun testGetLastTimestampOnEmptyTable() {
        val result = activityTransitionDao.getLastTimestamp()

        assertEquals(0, result)
    }

    @Test
    fun testGetLastTimestamp() {
        val dayFraction = "-01-2019"

        for (i in 11..30) {
            activityTransitionDao.insert(
                ActivityTransition(
                    i.toLong(),
                    formatter.parse("" + i + dayFraction),
                    DetectedActivity.ON_BICYCLE,
                    0,
                    formatter.parse("" + i + dayFraction).time + 10000,
                    false
                )
            )
        }

        val result = activityTransitionDao.getLastTimestamp()

        assertEquals(
            formatter.parse("30$dayFraction").time + 10000,
            result
        )
    }

    @Test
    fun testSetProcessed() {
        val dayFraction = "-01-2019"

        for (i in 11..30) {
            activityTransitionDao.insert(
                ActivityTransition(
                    i.toLong(),
                    formatter.parse("" + i + dayFraction),
                    DetectedActivity.ON_BICYCLE,
                    0,
                    formatter.parse("" + i + dayFraction).time + 10000,
                    Random.nextBoolean()
                )
            )
        }

        activityTransitionDao.setProcessed(
            formatter.parse("30$dayFraction").time + 10000
        )
        val activityTransitions = activityTransitionDao.getAll()

        assertEquals(true,
            activityTransitions
                .map(ActivityTransition::processed)
                .reduce { acc, b -> acc && b })
    }

    @Test
    fun testComputeOneNewActivity() {
        val activityTransition1 = ActivityTransition(
            0,
            Date(),
            5,
            0,
            Date().time + 100,
            true
        )
        val activityTransition2 = ActivityTransition(
            0,
            Date(),
            5,
            1,
            Date().time + 200,
            false
        )

        activityTransitionDao.insert(activityTransition1)
        activityTransitionDao.insert(activityTransition2)

        val result = activityTransitionDao.computeNewActivities()

        assertEquals(1, result.size)
        assertEquals(100, result.first().duration)
    }
}