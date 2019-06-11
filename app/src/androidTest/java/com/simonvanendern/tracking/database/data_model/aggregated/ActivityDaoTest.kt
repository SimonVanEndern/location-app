package com.simonvanendern.tracking.database.data_model.aggregated

import com.google.android.gms.location.DetectedActivity.*
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.waitForValue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat

class ActivityDaoTest : DatabaseTest() {

    var formatter = SimpleDateFormat("dd-MM-yyyy")
    private lateinit var activityDao: ActivityDao

    @Before
    fun init() {
        activityDao = db.activityDao()
    }

    @Test
    fun testActivitySimpleInsert() {
        val day = formatter.parse("01-01-2019")
        val start = day.time + 10000
        val duration = 350
        val activity =
            Activity(
                0,
                day,
                ON_BICYCLE,
                start,
                duration
            )
        val id = activityDao.insert(activity)
        val savedActivity = activityDao.getById(id.toInt())

        // We test for only one property but all except serverId should be equal
        assertEquals(duration, savedActivity.duration)
    }

    @Test
    fun testGet10RecentActivities() {
        val dayFraction = "-01-2019"

        for (i in 11..30) {
            activityDao.insert(
                Activity(
                    i,
                    formatter.parse("" + i + dayFraction),
                    ON_BICYCLE,
                    formatter.parse("" + i + dayFraction).time + 10000,
                    i * 11123
                )
            )
        }

        val result = activityDao.get10RecentActivities().waitForValue()
        assertEquals(10, result.size)
        assertEquals(30, result.first().id)
    }

    @Test
    fun testGetTotalTimeSpentOnActivity() {
        val day1 = formatter.parse("01-01-2019")
        val day2 = formatter.parse("03-01-2019")
        val day3 = formatter.parse("04-01-2019")

        val activities = listOf(
            Activity(
                0,
                day1,
                ON_BICYCLE,
                day1.time + 10000,
                500
            ), Activity(
                0,
                day2,
                ON_BICYCLE,
                day1.time + 20000,
                200
            ), Activity(
                0, day2,
                WALKING,
                day2.time + 30000,
                300
            ), Activity(
                0,
                day3,
                IN_VEHICLE,
                day3.time + 10000,
                1000
            ), Activity(
                0,
                day3,
                WALKING,
                day3.time + 20000,
                15
            )
        )

        activityDao.insertAll(activities)

        val timeSpentWalkingAllDays = activityDao
            .getTotalTimeSpentOnActivity(
                day1,
                day3,
                WALKING
            )
        val timeSpentWalkingFirstTwoDays = activityDao
            .getTotalTimeSpentOnActivity(
                day1,
                day2,
                WALKING
            )

        val timeSpentOnBicycleLastTwoDays = activityDao
            .getTotalTimeSpentOnActivity(
                day2,
                day3,
                ON_BICYCLE
            )

        assertEquals(315, timeSpentWalkingAllDays)
        assertEquals(300, timeSpentWalkingFirstTwoDays)
        assertEquals(200, timeSpentOnBicycleLastTwoDays)
    }
}

