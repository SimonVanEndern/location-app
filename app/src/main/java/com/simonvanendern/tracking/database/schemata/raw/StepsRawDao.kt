package com.simonvanendern.tracking.database.schemata.raw

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.simonvanendern.tracking.database.schemata.aggregated.Steps
import java.util.*

@Dao
interface StepsRawDao {

    @Insert(onConflict = REPLACE)
    fun insert(stepStatistic: StepsRaw)

    @Query("SELECT * FROM step_counter_table")
    fun getAll () : List<StepsRaw>

    @Query("SELECT steps FROM step_counter_table WHERE day = :day LIMIT 1")
    fun getSteps(day: Date): Int

    // For testing
    @Query("SELECT steps FROM step_counter_table ORDER BY day DESC LIMIT 10")
    fun get10RecentSteps (): LiveData<List<Int>>

    @Query("SELECT MAX(timestamp) FROM step_counter_table")
    fun getLastTimestamp() : Long

//    @Query("""SELECT *
//                    FROM step_counter_table
//                    WHERE timestamp =
//                    (SELECT MIN(timestamp) FROM step_counter_table)
//                    LIMIT 1""")
//    fun getFirstEntry() : StepsRaw

    @Query("""
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
        """)
    fun computeNewSteps() : List<Steps>

    @Query(
        """UPDATE step_counter_table SET processed = 1
                WHERE timestamp <= :lastTimestamp"""
    )
    fun setProcessed(lastTimestamp: Long)
}