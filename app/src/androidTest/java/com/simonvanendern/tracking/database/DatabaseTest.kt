package com.simonvanendern.tracking.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
abstract class DatabaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: LocationRoomDatabase

    fun getDb () : LocationRoomDatabase {
        return db
    }

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, LocationRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        LocationRoomDatabase.setDatabase(db)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}