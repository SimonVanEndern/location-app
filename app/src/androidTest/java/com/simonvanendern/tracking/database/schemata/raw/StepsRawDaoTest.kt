package com.simonvanendern.tracking.database.schemata.raw

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.database.DatabaseTest
import org.junit.Assert.assertEquals
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
        stepsRawDao = db.stepsRawDao()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSteps() {
        val day = formatter.parse("2019-01-01")
        val steps = 5000
        val stepsObject = StepsRaw(day.time, day, steps, false)
        stepsRawDao.insert(stepsObject)
        val result = stepsRawDao.getSteps(day)
        assertEquals(steps, result)
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
        assertEquals(steps1, result)
        val stepsObject2 = StepsRaw(day.time, day, steps2, false)
        stepsRawDao.insert(stepsObject2)
        result = stepsRawDao.getSteps(day)
        assertEquals(steps2, result)
    }

    @Test
    fun testGetLastTimestampOnEmptyTable() {
        val result = stepsRawDao.getLastTimestamp()

        assertEquals(0, result)
    }

    @Test
    fun testGetLastTimestamp() {
        val dayFraction = "2019-01-"

        for (i in 11..30) {
            stepsRawDao.insert(
                StepsRaw(
                    formatter.parse(dayFraction + i).time,
                    formatter.parse(dayFraction + i),
                    300,
                    false
                )
            )
        }

        val result = stepsRawDao.getLastTimestamp()

        assertEquals(
            formatter.parse(dayFraction + 30).time,
            result
        )
    }

    @Test
    fun computeNewSteps() {
        stepsRawDao = db.stepsRawDao()

        val day1 = "2019-01-02"
        val day2 = "2019-01-03"
        val day3 = "2019-01-04"

        val stepsRaw = listOf(
            StepsRaw(
                formatter.parse(day1).time,
                formatter.parse(day1),
                50,
                true
            ),
            StepsRaw(
                formatter.parse(day1).time + 10000,
                formatter.parse(day1),
                200,
                false
            ),
            StepsRaw(
                formatter.parse(day1).time + 30000,
                formatter.parse(day1),
                20,
                false
            ),
            StepsRaw(
                formatter.parse(day2).time + 200,
                formatter.parse(day2),
                40,
                false
            ),
            StepsRaw(
                formatter.parse(day3).time + 100,
                formatter.parse(day3),
                10,
                false
            )
        )

        stepsRawDao.insertAll(stepsRaw)

        val result = stepsRawDao.computeNewSteps()

        assertEquals(4, result.size)

        assertEquals(150, result[0].steps)
        assertEquals(formatter.parse(day1), result[0].day)

        assertEquals(20, result[1].steps)
        assertEquals(formatter.parse(day1), result[1].day)

        assertEquals(20, result[2].steps)
        assertEquals(formatter.parse(day1), result[2].day)

        assertEquals(10, result[3].steps)
        assertEquals(formatter.parse(day2), result[3].day)
    }
}