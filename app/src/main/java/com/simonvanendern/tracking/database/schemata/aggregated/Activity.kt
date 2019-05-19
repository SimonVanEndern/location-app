package com.simonvanendern.tracking.database.schemata.aggregated

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "activity_table")
data class Activity (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id : Int,
    @ColumnInfo(name = "day") val day : Date?,
    @ColumnInfo(name = "activity_type") val activityType : Int,
    @ColumnInfo(name = "start") val start : Long,
    @ColumnInfo(name = "duration") val duration : Int
)