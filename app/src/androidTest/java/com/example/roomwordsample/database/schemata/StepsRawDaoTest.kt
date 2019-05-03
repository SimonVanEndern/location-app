package com.example.roomwordsample.database.schemata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roomwordsample.database.DatabaseTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat

@RunWith(AndroidJUnit4::class)
class StepsRawDaoTest : DatabaseTest() {

    private lateinit var stepsRawDao: StepsRawDao

    private var formatter = SimpleDateFormat("yyyy-MM-dd")

    @Before
    fun init() {
        stepsRawDao = getDb().stepsRawDao()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSteps() {
        val day = formatter.parse("2019-01-01")
        val steps = 5000
        val stepsObject = StepsRaw(day.time, day, steps, false)
        stepsRawDao.insert(stepsObject)
        val result = stepsRawDao.getSteps(day)
        Assert.assertEquals(steps, result)
    }

    @Test
    @Throws(Exception::class)
    fun insertNewValueForSameDayOverwrites() {
        val day = formatter.parse("2019-01-01")
        val steps1 = 3000
        val steps2 = 4000
        val stepsObject1 = StepsRaw(day.time, day, steps1, false)
        stepsRawDao.insert(stepsObject1)
        var result = stepsRawDao.getSteps(day)
        Assert.assertEquals(steps1, result)
        val stepsObject2 = StepsRaw(day.time, day, steps2, false)
        stepsRawDao.insert(stepsObject2)
        result = stepsRawDao.getSteps(day)
        Assert.assertEquals(steps2, result)
    }
}