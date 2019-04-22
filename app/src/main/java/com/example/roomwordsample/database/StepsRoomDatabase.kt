package com.example.roomwordsample.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Steps::class], version = 1)
abstract class StepsRoomDatabase : RoomDatabase() {

    abstract fun stepsDao(): StepsDao
}