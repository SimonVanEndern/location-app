package com.simonvanendern.tracking.database.schemata.aggregated

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.database.schemata.aggregated.Steps
import com.simonvanendern.tracking.database.schemata.aggregated.StepsDao
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat

@RunWith(AndroidJUnit4::class)
class StepsDaoTest : DatabaseTest() {

    private lateinit var stepsDao: StepsDao

    private var formatter = SimpleDateFormat("yyyy-MM-dd")

    @Before
    fun init() {
        stepsDao = getDb().stepsDao()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSteps() {
        val day = formatter.parse("2019-01-01")
        val steps = 5000
        val stepsObject = Steps(day.time, day, steps)
        stepsDao.insert(stepsObject)
        val result = stepsDao.getTotalStepsByDay(day)
        Assert.assertEquals(steps, result)
    }

    @Test
    @Throws(Exception::class)
    fun insertNewValueForSameDayOverwrites() {
        val day = formatter.parse("2019-01-01")
        val steps1 = 3000
        val steps2 = 4000
        val stepsObject1 = Steps(day.time, day, steps1)
        stepsDao.insert(stepsObject1)
        var result = stepsDao.getTotalStepsByDay(day)
        Assert.assertEquals(steps1, result)
        val stepsObject2 = Steps(day.time, day, steps2)
        stepsDao.insert(stepsObject2)
        result = stepsDao.getTotalStepsByDay(day)
        Assert.assertEquals(steps2, result)
    }

    @Test
    @Throws(Exception::class)
    fun getAverageSteps() {
        val day1 = formatter.parse("2019-01-01")
        val day2 = formatter.parse("2019-02-01")
        val steps1 = 3000
        val steps2 = 4000
        val stepsObject1 = Steps(day1.time, day1, steps1)
        val stepsObject2 = Steps(day2.time, day2, steps2)
        stepsDao.insert(stepsObject1)
        stepsDao.insert(stepsObject2)
        val result = stepsDao.getAverageSteps(day1, day2)
        Assert.assertEquals((steps1 + steps2) / 2.toFloat(), result)
    }
}