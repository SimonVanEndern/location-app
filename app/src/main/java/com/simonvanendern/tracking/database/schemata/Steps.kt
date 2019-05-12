package com.simonvanendern.tracking.database.schemata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "steps_table")
data class Steps(
    @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp : Long,
    @ColumnInfo(name = "day") val day : Date,
    @ColumnInfo(name = "steps") val steps : Int
)
