package com.simonvanendern.tracking.database.schemata.raw

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.Query
import com.simonvanendern.tracking.database.schemata.aggregated.Activity
import java.util.*

@Dao
interface ActivityTransitionDao {

    @Insert(onConflict = ABORT)
    fun insert(activityTransition: ActivityTransition): Long

    @Insert
    fun insertAll (activityTransitions : List<ActivityTransition>)

    @Query("SELECT * FROM activity_transition_table")
    fun getAll(): List<ActivityTransition>

    @Query("SELECT * FROM activity_transition_table WHERE day = :day")
    fun getActivitiesByDay(day: Date?): List<ActivityTransition>

    // For testing
    @Query("SELECT * FROM activity_transition_table ORDER BY id DESC LIMIT 10")
    fun get10RecentActivityTransitions(): LiveData<List<ActivityTransition>>

    @Query("SELECT COALESCE(MAX(start),0) FROM activity_transition_table")
    fun getLastTimestamp(): Long

    @Query(
        """UPDATE activity_transition_table SET processed = 1
                WHERE start <= :lastTimestamp"""
    )
    fun setProcessed(lastTimestamp: Long)

    @Query(
        """
        SELECT 0 as id, at1.day, at1.activity_type, at1.start, at2.start - at1.start as duration
            FROM activity_transition_table at1, activity_transition_table at2
            WHERE at2.processed = 0
            AND at1.transition_type = 0
            AND at1.activity_type = at2.activity_type
            AND at1.transition_type != at2.transition_type
            AND at2.start = (SELECT MIN(start) FROM activity_transition_table WHERE start > at1.start)
    """
    )
    fun computeNewActivities(): List<Activity>
}