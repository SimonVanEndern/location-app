package com.example.roomwordsample.database

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roomwordsample.database.schemata.GPSData
import com.example.roomwordsample.database.schemata.GPSDataDao
import com.example.roomwordsample.database.schemata.GPSLocation
import com.example.roomwordsample.database.schemata.GPSLocationDao
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GPSDataDaoTest : DaoTest() {

    private lateinit var gpsDataDao: GPSDataDao
    private lateinit var gpsLocationDao: GPSLocationDao

    @Before
    fun init() {
        gpsDataDao = getDb().gPSDataDao()
        gpsLocationDao = getDb().gPSLocationDao()
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