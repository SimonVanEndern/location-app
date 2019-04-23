package com.example.roomwordsample.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [GPSLocation::class, GPSData::class, Activity::class, Trajectory::class, Steps::class], version = 1)
@TypeConverters(Converters::class)
abstract class LocationRoomDatabase : RoomDatabase() {

    abstract fun gPSLocationDao(): GPSLocationDao

    abstract fun gPSDataDao(): GPSDataDao

    abstract fun activityDao(): ActivityDao

    abstract fun trajectoryDao(): TrajectoryDao

    abstract fun stepsDao(): StepsDao
}