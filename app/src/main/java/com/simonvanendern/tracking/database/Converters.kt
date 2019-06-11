package com.simonvanendern.tracking.database

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.reduce as reduce1

/**
 * Converter class used to convert complex data types to basic data types storable in an SQL database
 */
class Converters {

    private var dateFormat = SimpleDateFormat("yyyy-MM-dd")

    /**
     * Builds a @see Date from a string according to dateFormat
     */
    @TypeConverter
    fun fromString(value: String): Date {
        return dateFormat.parse(value)
    }

    /**
     * Converts a @see Date to a String according to dateFormat
     */
    @TypeConverter
    fun fromDate(date: Date): String {
        return dateFormat.format(date)
    }

    /**
     * Builds a list from a string representation
     */
    @TypeConverter
    fun stringFromList(value: MutableList<Float>): String {
        return value.joinToString { it.toString() }
    }

    /**
     * Converts a list to a string representation
     */
    @TypeConverter
    fun listFromString(value: String): MutableList<Float> {
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
}