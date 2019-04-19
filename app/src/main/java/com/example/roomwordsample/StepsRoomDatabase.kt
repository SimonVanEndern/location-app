package com.example.roomwordsample

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [Steps::class], version = 1)
abstract class StepsRoomDatabase : RoomDatabase() {

    abstract fun stepsDao(): StepsDao
}