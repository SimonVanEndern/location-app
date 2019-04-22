package com.example.roomwordsample.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Activity::class], version = 1)
@TypeConverters(Converters::class)
abstract class ActivityRoomDatabase : RoomDatabase () {

    abstract fun activityDao () : ActivityDao
}