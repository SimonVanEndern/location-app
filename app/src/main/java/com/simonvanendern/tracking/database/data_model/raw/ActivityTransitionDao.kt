package com.simonvanendern.tracking.database.data_model.raw

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.Query
import com.simonvanendern.tracking.database.data_model.aggregated.Activity
import java.util.*

/**
 * The data access object / class for the activity_transition_table defined in @see ActivityTransition
 */
@Dao
interface ActivityTransitionDao {

    @Insert(onConflict = ABORT)
    fun insert(activityTransition: ActivityTransition): Long

    @Insert(onConflict = ABORT)
    fun insertAll(activityTransitions: List<ActivityTransition>)

    @Query("SELECT * FROM activity_transition_table")
    fun getAll(): List<ActivityTransition>

    @Query("SELECT * FROM activity_transition_table WHERE day = :day")
    fun getActivitiesByDay(day: Date?): List<ActivityTransition>

    /**
     * This method is rather for debugging and testing purposes.
     * It retrieves the 10 most recent entries of the activity_transition_table.
     */
    @Query("SELECT * FROM activity_transition_table ORDER BY id DESC LIMIT 10")
    fun get10RecentActivityTransitions(): LiveData<List<ActivityTransition>>

    /**
     * Retrieves the most recent timestamp of all available activity transitions
     */
    @Query("SELECT COALESCE(MAX(start),0) FROM activity_transition_table")
    fun getLastTimestamp(): Long

    /**
     * Sets the processed flag for each entry with a timestamp value below or equal to @param timestamp
     * @param lastTimestamp The timestamp in milliseconds
     */
    @Query(
        """UPDATE activity_transition_table SET processed = 1
                WHERE start <= :lastTimestamp"""
    )
    fun setProcessed(lastTimestamp: Long)

    /**
     * Matches two subsequent activity transitions where the first one is of type ENTER
     * and the second one of type EXIT
     */
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