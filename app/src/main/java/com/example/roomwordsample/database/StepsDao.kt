package com.example.roomwordsample.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.util.*

@Dao
interface StepsDao {

    @Insert(onConflict = REPLACE)
    fun insert(stepStatistic: Steps)

    @Query("SELECT steps FROM step_counter_table WHERE day = :day LIMIT 1")
    fun getSteps(day: Date): Int

    @Query("SELECT AVG(steps) FROM step_counter_table WHERE day BETWEEN :startDayInclusive AND :endDayInclusive")
    fun getAverageSteps(startDayInclusive: Date, endDayInclusive: Date): Float

    // For testing
    @Query("SELECT steps FROM step_counter_table ORDER BY day ASC LIMIT 10")
    fun get10RecentSteps (): LiveData<List<Int>>
}