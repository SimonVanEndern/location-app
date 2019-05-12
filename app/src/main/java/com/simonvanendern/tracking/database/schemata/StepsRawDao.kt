package com.simonvanendern.tracking.database.schemata

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
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
}