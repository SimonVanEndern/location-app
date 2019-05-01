package com.example.roomwordsample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.roomwordsample.database.ActivityTransition
import com.example.roomwordsample.database.ActivityTransitionDao
import com.example.roomwordsample.database.LocationRoomDatabase
import com.google.android.gms.location.DetectedActivity
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.text.SimpleDateFormat

@RunWith(AndroidJUnit4::class)
class ActivityTransitionDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    var formatter = SimpleDateFormat("dd-MM-yyyy")

    private lateinit var activityTransitionDao: ActivityTransitionDao
    private lateinit var db: LocationRoomDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, LocationRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        activityTransitionDao = db.activityDao()
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
        val day2 = formatter.parse("01-01-2019")
        val start2 = day.time + 40000L
        val activity1 = ActivityTransition(0, day, DetectedActivity.ON_FOOT, 0, start)
        val activity2 = ActivityTransition(0, day2, DetectedActivity.ON_BICYCLE, 0, start2)
        activityTransitionDao.insert(activity1)
        activityTransitionDao.insert(activity2)
        val activities = activityTransitionDao.getActivitiesByDay(day)
        Assert.assertEquals(2, activities.size)
    }
}