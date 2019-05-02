package com.example.roomwordsample.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roomwordsample.database.schemata.GPSLocation
import com.example.roomwordsample.database.schemata.GPSLocationDao
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GPSLocationDaoTest : DaoTest() {

    private lateinit var gpsLocationDao: GPSLocationDao

    @Before
    fun init() {
        gpsLocationDao = getDb().gPSLocationDao()
    }

    @Test
    fun testInsert() {
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