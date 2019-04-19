package com.example.roomwordsample

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "step_counter_table")
data class Steps(@PrimaryKey @ColumnInfo(name = "day") val day : String,
                 @ColumnInfo(name = "steps") val steps: Int)
