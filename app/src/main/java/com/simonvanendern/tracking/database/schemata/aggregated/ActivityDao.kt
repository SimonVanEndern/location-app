package com.simonvanendern.tracking.database.schemata.aggregated

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface ActivityDao {

    @Insert
    fun insert (activity : Activity) : Long

    @Query("SELECT * FROM activity_table")
    fun getAll () : List<Activity>

    @Query("SELECT * FROM activity_table WHERE id = :id LIMIT 1")
    fun getById (id : Int) : Activity

    // For testing
    @Query("SELECT * FROM activity_table ORDER BY start DESC LIMIT 10")
    fun get10RecentActivities(): LiveData<List<Activity>>

    // TODO: correct implementation
    @Query(
        """SELECT 0 FROM activity_transition_table
         WHERE activity_type = :activity AND
         day BETWEEN :start AND :end"""
    )
    fun getTotalTimeSpentOnActivity(start: Date?, end: Date?, activity: Int): Long

    @Insert
    fun insertAll (activities : List<Activity>)
}