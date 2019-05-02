package com.example.roomwordsample.database.schemata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrajectoryDao {

    @Insert
    fun insert (trajectory : Trajectory) : Long

    @Query("SELECT * FROM trajectory_table WHERE id = :id LIMIT 1")
    fun getById (id : Long) : Trajectory
}