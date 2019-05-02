package com.example.roomwordsample.database.schemata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roomwordsample.database.DatabaseTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat

@RunWith(AndroidJUnit4::class)
class StepsDaoTest : DatabaseTest() {

    private lateinit var stepsDao: StepsDao

    var formatter = SimpleDateFormat("dd-MM-yyyy")

    @Before
    fun init() {
        stepsDao = getDb().stepsDao()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSteps() {
        val day = formatter.parse("01-01-2019")
        val steps = 5000
        val stepsObject = Steps(day, steps)
        stepsDao.insert(stepsObject)
        val result = stepsDao.getSteps(day)
        Assert.assertEquals(steps, result)
    }

    @Test
    @Throws(Exception::class)
    fun insertNewValueForSameDayOverwrites() {
        val day = formatter.parse("01-01-2019")
        val steps1 = 3000
        val steps2 = 4000
        val stepsObject1 = Steps(day, steps1)
        stepsDao.insert(stepsObject1)
        var result = stepsDao.getSteps(day)
        Assert.assertEquals(steps1, result)
        val stepsObject2 = Steps(day, steps2)
        stepsDao.insert(stepsObject2)
        result = stepsDao.getSteps(day)
        Assert.assertEquals(steps2, result)
    }

    @Test
    @Throws(Exception::class)
    fun getAverageSteps() {
        val day1 = formatter.parse("01-01-2019")
        val day2 = formatter.parse("02-01-2019")
        val steps1 = 3000
        val steps2 = 4000
        val stepsObject1 = Steps(day1, steps1)
        val stepsObject2 = Steps(day2, steps2)
        stepsDao.insert(stepsObject1)
        stepsDao.insert(stepsObject2)
        val result = stepsDao.getAverageSteps(day1, day2)
        Assert.assertEquals((steps1 + steps2) / 2.toFloat(), result)
    }
}