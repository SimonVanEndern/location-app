package com.example.roomwordsample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.roomwordsample.database.GPSLocation
import com.example.roomwordsample.database.GPSLocationDao
import com.example.roomwordsample.database.LocationRoomDatabase
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class GPSLocationDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var gpsLocationDao: GPSLocationDao
    private lateinit var db: LocationRoomDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, LocationRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        gpsLocationDao = db.gPSLocationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        db.close()
    }

    @Test
    fun testInsert () {
        val longitude = 48.1634239F
        val latitude = 11.5637839F
        val speed = 4.5F
        val gpsLocation = GPSLocation(0, longitude, latitude, speed)
        val result = gpsLocationDao.insert(gpsLocation)
        // TODO: Use random identifiers for more security
        Assert.assertTrue(result > 0)
        val retrieved = gpsLocationDao.getById(result)
        Assert.assertEquals(latitude, retrieved.latitude)
        Assert.assertEquals(longitude, retrieved.longitude)
        Assert.assertEquals(speed, retrieved.speed)
    }
}