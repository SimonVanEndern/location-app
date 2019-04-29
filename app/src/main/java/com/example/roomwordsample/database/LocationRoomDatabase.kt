package com.example.roomwordsample.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GPSLocation::class, GPSData::class, Activity::class, Trajectory::class, Steps::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class LocationRoomDatabase : RoomDatabase() {

    abstract fun gPSLocationDao(): GPSLocationDao

    abstract fun gPSDataDao(): GPSDataDao

    abstract fun activityDao(): ActivityDao

    abstract fun trajectoryDao(): TrajectoryDao

    abstract fun stepsDao(): StepsDao

    companion object {
        @Volatile
        private var INSTANCE: LocationRoomDatabase? = null

        fun getDatabase(
            context: Context
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
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}