package com.example.roomwordsample.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Steps::class], version = 1)
@TypeConverters(Converters::class)
abstract class StepsRoomDatabase : RoomDatabase() {

    abstract fun stepsDao(): StepsDao
}