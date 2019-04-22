package com.example.roomwordsample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.roomwordsample.database.Steps
import com.example.roomwordsample.database.StepsDao
import com.example.roomwordsample.database.StepsRoomDatabase
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class StepsDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var stepsDao: StepsDao
    private lateinit var db: StepsRoomDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, StepsRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        stepsDao = db.stepsDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSteps() {
        val day = "01-01-2019"
        val steps = 5000
        val stepsObject = Steps(day, steps)
        stepsDao.insert(stepsObject)
        val result = stepsDao.getSteps(day)
        Assert.assertEquals(steps, result)
    }
}