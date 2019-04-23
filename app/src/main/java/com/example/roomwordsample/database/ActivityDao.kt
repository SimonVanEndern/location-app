package com.example.roomwordsample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.android.gms.location.DetectedActivity
import java.util.*

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(activity: Activity)

    @Query("SELECT * FROM activity_table WHERE day = :day")
    fun getActivitiesByDay(day: Date?): List<Activity>

    @Query(
        """SELECT SUM(duration) FROM activity_table
         WHERE activity = :activity AND
         day BETWEEN :start AND :end"""
    )
    fun getTotalTimeSpentOnActivity(start: Date?, end: Date?, activity: DetectedActivity) : Long
}