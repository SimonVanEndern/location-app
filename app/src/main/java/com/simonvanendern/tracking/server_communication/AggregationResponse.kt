package com.simonvanendern.tracking.server_communication

class AggregationResponse(
    val serverId: String,
    val nextUser: String,
    val encryptionKey: String,
    val encryptedRequest: String,
    val iv: String
)
