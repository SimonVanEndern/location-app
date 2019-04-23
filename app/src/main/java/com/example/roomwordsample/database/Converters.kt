package com.example.roomwordsample.database

import androidx.room.TypeConverter
import com.google.android.gms.location.DetectedActivity
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromActivity(activity: DetectedActivity): Int {
        return activity.type
    }

    @TypeConverter
    fun activityFromInt(value: Int): DetectedActivity {
        // TODO: Change to incorporate confidence?
        return DetectedActivity(value, 1)
    }
}