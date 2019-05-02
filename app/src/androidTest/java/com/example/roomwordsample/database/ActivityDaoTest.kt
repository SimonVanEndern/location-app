package com.example.roomwordsample.database

import com.example.roomwordsample.database.schemata.Activity
import com.example.roomwordsample.database.schemata.ActivityDao
import com.google.android.gms.location.DetectedActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat

class ActivityDaoTest : DaoTest() {

    var formatter = SimpleDateFormat("dd-MM-yyyy")
    private lateinit var activityDao : ActivityDao

    @Before
    fun init () {
        activityDao = getDb().activityDao()
    }

    @Test
    fun testActivitySimpleInsert() {
        val day = formatter.parse("01-01-2019")
        val start = day.time + 10000
        val duration = 350
        val activity =
            Activity(0, day, DetectedActivity.ON_BICYCLE, start, duration)
        val id = activityDao.insert(activity)
        val savedActivity = activityDao.getById(id.toInt())

        // We test for only one property but all except id should be equal
        Assert.assertEquals(duration, savedActivity.duration)
    }
}

