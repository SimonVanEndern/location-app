package com.simonvanendern.tracking.database.data_model.aggregated

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

/**
 * The data access object / class for the steps_table defined in @see Activity
 */
@Dao
interface StepsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(steps: Steps): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(steps: List<Steps>)

    @Query("SELECT * FROM steps_table")
    fun getAll(): List<Steps>

    /**
     * This method is rather for debugging and testing purposes.
     * It retrieves the 10 most recent entries of the steps_table.
     */
    @Query("SELECT * FROM steps_table ORDER BY day DESC, timestamp DESC LIMIT 10")
    fun get10RecentSteps(): LiveData<List<Steps>>

    /**
     * @param day The day of interest
     * @return The total number of steps walked on the respective day
     */
    @Query("SELECT SUM(steps) FROM steps_table WHERE day = :day GROUP BY day")
    fun getTotalStepsByDay(day: Date): Int

    /**
     * @param startDayInclusive The start day of the time period
     * @param endDayInclusive The end day of the time period.
     * Use getTotalStepsByDay if start and end date is the same.
     * @return The average number of steps walked in the specified time period.
     */
    @Query(
        """
        SELECT SUM(steps) / COUNT(DISTINCT day)
        FROM steps_table
        WHERE day BETWEEN :startDayInclusive AND :endDayInclusive"""
    )
    fun getAverageSteps(startDayInclusive: Date, endDayInclusive: Date): Float
}