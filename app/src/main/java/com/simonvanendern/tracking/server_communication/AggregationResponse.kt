package com.simonvanendern.tracking.server_communication

/**
 * POJO for @see WebService.
 * The names and values of this class resemble the JSON names and values used for
 * server communication
 */
class AggregationResponse(
    val serverId: String,
    val nextUser: String,
    val encryptionKey: String,
    val encryptedRequest: String,
    val iv: String
)