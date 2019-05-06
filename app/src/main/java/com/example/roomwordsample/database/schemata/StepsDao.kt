package com.example.roomwordsample.database.schemata

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface StepsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(steps: Steps): Long

    @Query("SELECT * FROM steps_table")
    fun getAll () : List<Steps>

    @Query("SELECT SUM(steps) FROM steps_table WHERE day = :day GROUP BY day")
    fun getTotalStepsByDay(day: Date): Int

    @Query(
        """
        SELECT SUM(steps) / COUNT(DISTINCT day)
        FROM steps_table
        WHERE day BETWEEN :startDayInclusive AND :endDayInclusive"""
    )
    fun getAverageSteps(startDayInclusive: Date, endDayInclusive: Date): Float

    // For testing
    @Query("SELECT steps FROM steps_table ORDER BY day DESC, timestamp DESC LIMIT 10")
    fun get10RecentSteps (): LiveData<List<Int>>
}