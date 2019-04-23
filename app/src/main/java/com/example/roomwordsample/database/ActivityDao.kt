package com.example.roomwordsample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert (activity : Activity)

    @Query("SELECT * FROM activity_table WHERE day = :day")
    fun getActivitiesByDay (day : Date?) : List<Activity>
}