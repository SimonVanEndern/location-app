package com.example.roomwordsample.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "step_counter_table")
data class Steps(
    @PrimaryKey @ColumnInfo(name = "day") val day: Date?,
    @ColumnInfo(name = "steps") val steps: Int
)
