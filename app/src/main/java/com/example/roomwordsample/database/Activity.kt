package com.example.roomwordsample.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "activity_table")
data class Activity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "day") val day: Date?,
    @ColumnInfo(name = "activity") val activity: Int,
    @ColumnInfo(name = "start") val start: Long,
    @ColumnInfo(name = "duration") val duration: Long
)