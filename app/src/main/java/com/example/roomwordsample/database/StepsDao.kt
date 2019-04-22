package com.example.roomwordsample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StepsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stepStatistic: Steps)

    @Query("SELECT steps FROM step_counter_table WHERE day = :day LIMIT 1")
    fun getSteps(day: String): Int

    @Query("SELECT AVG(steps) FROM step_counter_table WHERE day >= :startDayInclusive AND day >= :endDayInclusive")
    fun getAverageSteps(startDayInclusive: String, endDayInclusive: String): Int
}