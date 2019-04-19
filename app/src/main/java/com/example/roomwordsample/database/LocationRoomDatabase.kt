package com.example.roomwordsample.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [GPSLocation::class, GPSData::class], version = 1)
abstract class LocationRoomDatabase : RoomDatabase () {

    abstract fun gPSLocationDao () : GPSLocationDao

    abstract fun gPSDataDao () : GPSDataDao
}