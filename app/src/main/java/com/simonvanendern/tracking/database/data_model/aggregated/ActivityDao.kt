package com.simonvanendern.tracking.database.data_model.aggregated

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

/**
 * The data access object / class for the activity_table defined in @link Activity
 */
@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(activity: Activity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(activities: List<Activity>)

    @Query("SELECT * FROM activity_table")
    fun getAll(): List<Activity>

    @Query("SELECT * FROM activity_table WHERE id = :id LIMIT 1")
    fun getById(id: Int): Activity

    /**
     * This method is rather for debugging and testing purposes.
     * It retrieves the 10 most recent entries of the activity_table.
     */
    @Query("SELECT * FROM activity_table ORDER BY start DESC LIMIT 10")
    fun get10RecentActivities(): LiveData<List<Activity>>

    /**
     * @param start The start date (usually as of 00:00 o'clock)
     * @param end The end date (usually as of 00:00 o'clock)
     * @param activity The activity type as of @see DetectedActivity
     * @return The total time in nano seconds spent on this activity during the specified time period.
     */
    @Query(
        """SELECT SUM(duration) FROM activity_table
         WHERE activity_type = :activity AND
         day BETWEEN :start AND :end"""
    )
    fun getTotalTimeSpentOnActivity(start: Date, end: Date, activity: Int): Long
}