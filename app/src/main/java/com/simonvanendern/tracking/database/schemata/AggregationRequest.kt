package com.simonvanendern.tracking.database.schemata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "aggregationRequest")
data class AggregationRequest (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")  val id : Int,
    @ColumnInfo(name = "serverSideId") val serverId : String,
    @ColumnInfo(name = "nextUser") val nextUser : String,
    @ColumnInfo(name = "type") val type : String,
    @ColumnInfo(name = "n") val n : Int,
    @ColumnInfo(name = "value") val value : Float,
    @ColumnInfo(name = "start") val start : Date,
    @ColumnInfo(name = "end") val end : Date,
    @ColumnInfo(name = "incoming") val incoming : Boolean,
    @ColumnInfo(name = "valueList") val valueList : MutableList<Float>
) {
//    override fun equals(other: Any?): Boolean{
//        if (this === other) return true
//        if (other?.javaClass != javaClass) return false
//
//        other as AggregationRequest
//
//        if (!Arrays.equals(data, other.data)) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int{
//        return Arrays.hashCode(data)
//    }
}