package com.simonvanendern.tracking.database

import androidx.room.TypeConverter
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.joinAll
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.reduce as reduce1

class Converters {

    var dateFormat = SimpleDateFormat("yyyy-MM-dd")

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun fromString(value: String): Date {
        return dateFormat.parse(value)
    }

    @TypeConverter
    fun fromDate(date: Date): String {
        return dateFormat.format(date)
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

    @TypeConverter
    fun StringFromList(value : MutableList<Float>) : String {
        return value.joinToString { it.toString() }
    }

    @TypeConverter
    fun ListFromString (value : String) : MutableList<Float> {
        val strings = value.split(", ")
        if (strings.size == 1) {
            if (strings[0] == "") {
                return mutableListOf()
            }
        }
        if (strings.isEmpty()) {
            return mutableListOf()
        }
        return (strings.map { s -> s.toFloat() }).toMutableList()

    }

//    @TypeConverter
//    fun aggregationRequestFromDatabase (value : AggregationRequest) : AverageAggregationRequest<*> {
//        val json = value.data.toString()
//
//        val obj = Gson().fromJson(json, JsonObject::class.java)
//        val type = obj.get("type").asString
//        return when (type) {
//            "steps" -> {
//                val test = object : TypeToken<AverageAggregationRequest<AverageStepCount>>(){}.type
//                Gson().fromJson<AverageAggregationRequest<*>>(json, test)
//            }
//            else -> {
//                val test = object : TypeToken<AverageAggregationRequest<AverageTimeInActivity>>(){}.type
//                Gson().fromJson<AverageAggregationRequest<*>>(json, test)
//            }
//        }
//    }

//    @TypeConverter
//    fun <T : Datatype> aggregationRequestToJson(value : AverageAggregationRequest<*>) : String {
//        val test = object : TypeToken<AverageAggregationRequest<T>>(){}.type
//
//        return Gson().toJson(value, test)
//    }
}