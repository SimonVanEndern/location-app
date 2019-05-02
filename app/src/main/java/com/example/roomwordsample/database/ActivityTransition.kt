package com.example.roomwordsample.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "activity_transition_table")
data class ActivityTransition(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "day") val day: Date?,
    @ColumnInfo(name = "activity_type") val activityType: Int,
    @ColumnInfo(name = "transition_type") val transitionType: Int,
    @ColumnInfo(name = "start") val start: Long,
    @ColumnInfo(name = "processed") val processed : Boolean
)