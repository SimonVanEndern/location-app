package com.example.roomwordsample.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

// TODO: Change tableName and migration scripts
@Entity(tableName = "activities_table")
data class Activity (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id : Int,
    @ColumnInfo(name = "day") val day : Date?,
    @ColumnInfo(name = "activity_type") val activityType : Int,
    @ColumnInfo(name = "start") val start : Long,
    @ColumnInfo(name = "duration") val duration : Int
)