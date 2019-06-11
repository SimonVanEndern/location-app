package com.simonvanendern.tracking.database.schemata.raw

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.simonvanendern.tracking.database.schemata.aggregated.Steps
import java.util.*

/**
 * The data access object / class for the step_counter_table defined in @see StepsRaw
 */
@Dao
interface StepsRawDao {

    @Insert(onConflict = REPLACE)
    fun insert(stepStatistic: StepsRaw)

    @Insert(onConflict = REPLACE)
    fun insertAll(steps: List<StepsRaw>)

    @Query("SELECT * FROM step_counter_table")
    fun getAll(): List<StepsRaw>

    @Query("SELECT steps FROM step_counter_table WHERE day = :day LIMIT 1")
    fun getStepsByDay(day: Date): Int

    /**
     * Retrieves the most recent timestamp of all available step counter entries
     */
    @Query("SELECT MAX(timestamp) FROM step_counter_table")
    fun getLastTimestamp(): Long

    /**
     * Sets the processed flag for each entry with a timestamp value below or equal to @param timestamp
     * @param lastTimestamp The timestamp in milliseconds
     */
    @Query(
        """UPDATE step_counter_table SET processed = 1
                WHERE timestamp <= :lastTimestamp"""
    )
    fun setProcessed(lastTimestamp: Long)

    /**
     * Computes new entries for the steps_table.
     * If there are subsequent entries with the step counter value of the first entry being
     * below the second one, the difference in steps is computed and linked to the timestamp of the first.
     * If the step counter value of the first of two subsequent step counter values is higher than
     * the second, a reboot apparently took place in between and the difference is calculated by
     * subtracting the second counter value from the first.
     */
    @Query(
        """
            SELECT s1.timestamp, s1.day, s2.steps - s1.steps AS steps
            FROM step_counter_table AS s1, step_counter_table AS s2
            WHERE s2.processed = 0
            AND s2.steps > s1.steps
            AND s2.timestamp =
            (SELECT MIN(timestamp) FROM step_counter_table WHERE timestamp > s1.timestamp)
            UNION
            SELECT s1.timestamp, s1.day, s2.steps
            FROM step_counter_table AS s1, step_counter_table AS s2
            WHERE s2.processed = 0
            AND s2.steps < s1.steps
            AND s2.timestamp =
            (SELECT MIN(timestamp) FROM step_counter_table WHERE timestamp > s1.timestamp);
        """
    )
    fun computeNewSteps(): List<Steps>
}