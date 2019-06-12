package com.simonvanendern.tracking.server_communication

import java.util.*

/**
 * POJO for @see WebService.
 * The names and values of this class resemble the JSON names and values used for
 * server communication
 */
class AggregationRequest(
    val serverId: String,
    val nextUser: String?,
    val type: String,
    val n: Int,
    val value: Float,
    val start: Date,
    val end: Date,
    val valueList: MutableList<Float>
)