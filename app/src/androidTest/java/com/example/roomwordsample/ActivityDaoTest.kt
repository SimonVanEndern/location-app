package com.example.roomwordsample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.roomwordsample.database.Activity
import com.example.roomwordsample.database.ActivityDao
import com.example.roomwordsample.database.LocationRoomDatabase
import com.google.android.gms.location.DetectedActivity
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.text.SimpleDateFormat

@RunWith(AndroidJUnit4::class)
class ActivityDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    var formatter = SimpleDateFormat("dd-MM-yyyy")

    private lateinit var activityDao: ActivityDao
    private lateinit var db: LocationRoomDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, LocationRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        activityDao = db.activityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertTrajectory() {
        val day = formatter.parse("01-01-2019")
        val start = day.time + 10000
        val duration = 10000L
        val day2 = formatter.parse("01-01-2019")
        val start2 = day.time + 40000L
        val duration2 = 20000L
        val activity1 = Activity(0, day, DetectedActivity.ON_FOOT, start, duration)
        val activity2 = Activity(0, day2, DetectedActivity.ON_BICYCLE, start2, duration2)
        activityDao.insert(activity1)
        activityDao.insert(activity2)
        val activities = activityDao.getActivitiesByDay(day)
        Assert.assertEquals(2, activities.size)
    }
}