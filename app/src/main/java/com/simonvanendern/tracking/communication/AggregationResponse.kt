package com.simonvanendern.tracking.communication

class AggregationResponse(
    val serverId: String,
    val nextUser: String,
    val encryptionKey: String,
    val encryptedRequest: String,
    val iv: String
)
