package com.simonvanendern.tracking.database.data_model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * This class saves aggregation requests used in order to communicate with the server.
 * The fields correspond to the fields of the aggregation request passed from the server
 * to the app and vice versa.
 * The incoming flag determines whether it is an incoming aggregation request received from
 * the server or an outgoing request.
 * After processing aggregation requests, they are persisted as outgoing aggregation requests
 * until they are send to the server and a confirmation is received from the server.
 */
@Entity(tableName = "aggregation_request_table")
data class AggregationRequest(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "serverSideId") val serverId: String,
    @ColumnInfo(name = "nextUser") val nextUser: String?,
    @ColumnInfo(name = "start") val start: Date,
    @ColumnInfo(name = "end") val end: Date,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "n") val n: Int,
    @ColumnInfo(name = "value") val value: Float,
    @ColumnInfo(name = "valueList") val valueList: MutableList<Float>,
    @ColumnInfo(name = "incoming") val incoming: Boolean
)