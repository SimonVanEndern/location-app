package com.example.roomwordsample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface StepsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stepStatistic: Steps)

    @Query("SELECT steps FROM step_counter_table WHERE day = :day LIMIT 1")
    fun getSteps(day: Date): Int

    @Query("SELECT AVG(steps) FROM step_counter_table WHERE day BETWEEN :startDayInclusive AND :endDayInclusive")
    fun getAverageSteps(startDayInclusive: Date, endDayInclusive: Date): Float
}