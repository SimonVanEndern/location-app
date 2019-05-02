package com.example.roomwordsample.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActivityDao {

    @Insert
    fun insert (activity : Activity) : Long

    @Query("SELECT * FROM activities_table WHERE id = :id LIMIT 1")
    fun getById (id : Int) : Activity
}