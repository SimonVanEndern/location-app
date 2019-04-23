package com.example.roomwordsample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.roomwordsample.database.*
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.DetectedActivity
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TrajectoryDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var trajectoryDao: TrajectoryDao
    private lateinit var gpsLocationDao : GPSLocationDao
    private lateinit var db: LocationRoomDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, LocationRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        trajectoryDao = db.trajectoryDao()
        gpsLocationDao = db.gPSLocationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertTrajectory() {
        val latitude1 = 10.1F
        val longitude1 = 10.2F
        val speed1 = 3.3F
        val latitude2 = 4.4F
        val longitude2 = 4.5F
        val speed2 = 5.5F
        val gpsLocation1 = GPSLocation(0, longitude1, latitude1, speed1)
        val gpsLocation2 = GPSLocation(0, longitude2, latitude2, speed2)
        val id1 = gpsLocationDao.insert(gpsLocation1)
        val id2 = gpsLocationDao.insert(gpsLocation2)
        val startTime = 10000L
        val endTime = 20000L
        val activity = DetectedActivity.ON_FOOT
        val trajectory = Trajectory(0, startTime, endTime, id1, id2, activity)
        val trajectoryId = trajectoryDao.insert(trajectory)
        Assert.assertEquals(trajectory.start, trajectoryDao.getById(trajectoryId).start)
    }
}