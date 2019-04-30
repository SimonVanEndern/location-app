package com.example.roomwordsample.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Database(
    entities = [GPSLocation::class, GPSData::class, Activity::class, Trajectory::class, Steps::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class LocationRoomDatabase : RoomDatabase() {

    private class LocationRoomDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.activityDao())
                }
            }
        }

        fun populateDatabase(activityDao: ActivityDao) {
//            wordDao.deleteAll()

            val activity = Activity(0, Date(), 5, 1000, 1100)
            activityDao.insert(activity)
        }
    }

    abstract fun gPSLocationDao(): GPSLocationDao

    abstract fun gPSDataDao(): GPSDataDao

    abstract fun activityDao(): ActivityDao

    abstract fun trajectoryDao(): TrajectoryDao

    abstract fun stepsDao(): StepsDao

    companion object {
        @Volatile
        private var INSTANCE: LocationRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): LocationRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocationRoomDatabase::class.java,
                    "Location_database"
                ).addCallback(LocationRoomDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}