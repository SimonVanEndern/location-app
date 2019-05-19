package com.simonvanendern.tracking.communication

import java.util.*

class AggregationRequest(
    val serverId: String,
    val nextUser: String,
    val type : String,
    val n : Int,
    val value : Float,
    val start : Date,
    val end : Date
)
