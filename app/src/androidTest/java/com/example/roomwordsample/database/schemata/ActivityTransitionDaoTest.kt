package com.example.roomwordsample.database.schemata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roomwordsample.database.DatabaseTest
import com.google.android.gms.location.DetectedActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat

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
        Assert.assertEquals(2, activities.size)
    }
}