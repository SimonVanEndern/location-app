package com.example.roomwordsample

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface StepsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stepStatistic: Steps)

    @Query("SELECT * FROM step_counter_table WHERE day = :day LIMIT 1")
    fun getSteps(day: String): Int

    @Query("SELECT AVG(steps) FROM step_counter_table WHERE day >= :startDayInclusive AND day >= :endDayInclusive")
    fun getAverageSteps(startDayInclusive: String, endDayInclusive: String): Int
}