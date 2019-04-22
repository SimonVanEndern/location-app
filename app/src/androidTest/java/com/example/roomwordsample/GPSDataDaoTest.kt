package com.example.roomwordsample

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.roomwordsample.database.*
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class GPSDataDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var gpsDataDao: GPSDataDao
    private lateinit var gpsLocationDao: GPSLocationDao
    private lateinit var db: LocationRoomDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, LocationRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        gpsDataDao = db.gPSDataDao()
        gpsLocationDao = db.gPSLocationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertGPSData() {
        val location = GPSLocation(0, 10.4F, 10.5F, 3.4F)
        val id = gpsLocationDao.insert(location)
        val timestamp = 10000L
        gpsDataDao.insert(GPSData(id, timestamp))
        val result = gpsDataDao.getByTimestamp(timestamp)
        Assert.assertEquals(location.longitude, result.longitude)
        Assert.assertEquals(location.latitude, result.latitude)
        Assert.assertEquals(location.speed, result.speed)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testInsertGPSDataViolatingReferenceConstraint() {
        val timestamp = 1000L
        val id = 10L
        gpsDataDao.insert(GPSData(id, timestamp))
    }
}