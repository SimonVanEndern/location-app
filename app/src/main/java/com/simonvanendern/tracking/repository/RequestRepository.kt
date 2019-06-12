package com.simonvanendern.tracking.repository

import android.util.Base64
import com.simonvanendern.tracking.server_communication.AggregationRequest
import com.simonvanendern.tracking.server_communication.AggregationResponse
import com.simonvanendern.tracking.server_communication.User
import com.simonvanendern.tracking.server_communication.WebService
import com.simonvanendern.tracking.database.TrackingDatabase
import org.json.JSONObject
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling the requests used for server communication
 */
@Singleton
class RequestRepository @Inject constructor(
    db: TrackingDatabase,
    private val webService: WebService
) {
    private val aggregationRequestDao = db.aggregationRequestDao()

    fun createUser(userId: String): User? {
        return webService.createUser(User(userId, "")).execute().body()
    }

    /**
     * Retrieves all requests waiting to be processed by this user from the server
     * and stores them locally.
     */
    fun getPendingRequests(
        userId: String,
        pw: String
    ): List<com.simonvanendern.tracking.database.data_model.AggregationRequest> {

        val newRequests = webService.getRequestsForUser(userId, pw).execute().body() ?: emptyList()
        for (request in newRequests) {
            aggregationRequestDao.insert(
                com.simonvanendern.tracking.database.data_model.AggregationRequest(
                    0,
                    request.serverId,
                    request.nextUser,
                    request.start,
                    request.end,
                    request.type,
                    request.n,
                    request.value,
                    request.valueList,
                    true
                )
            )
        }

        return aggregationRequestDao.getAllIncomingAggregationRequests()
    }

    fun insertRequestResult(res: com.simonvanendern.tracking.database.data_model.AggregationRequest) {
        aggregationRequestDao.insert(res)
    }

    fun deletePendingRequest(req: com.simonvanendern.tracking.database.data_model.AggregationRequest) {
        aggregationRequestDao.delete(req)
    }

    /**
     * Sends all pending requests that have been processed and stored
     * to the server and deletes the request if the server acknowledges to
     * have received the request.
     */
    fun sendOutResults() {
        val requests = aggregationRequestDao.getAllPendingOutgoingAggregationRequests()
        for (res in requests) {
            if (res.nextUser == null) {
                if (webService.insertAggregationResult(
                        AggregationRequest(
                            res.serverId,
                            res.nextUser,
                            res.type,
                            res.n,
                            res.value,
                            res.start,
                            res.end,
                            res.valueList
                        )
                    ).execute().isSuccessful
                ) {
                    aggregationRequestDao.delete(res)
                }
            } else {
                if (webService.forwardAggregationRequest(
                        generateResponse(res)
                    ).execute().isSuccessful
                ) {
                    aggregationRequestDao.delete(res)
                }
            }
        }
    }

    /**
     * Encrypts the processed aggregation request with the public key of the next user
     * Encryption is hybrid - the aggregation request in JSON format itself is encrypted
     * with a synchronous key (AES) and this one is encrypted with the public key.
     */
    private fun generateResponse(request: com.simonvanendern.tracking.database.data_model.AggregationRequest): AggregationResponse {
        val generator = KeyGenerator.getInstance("AES")
        generator.init(256)
        val key = generator.generateKey()

        val formatter = SimpleDateFormat("yyyy-MM-dd")

        val json = JSONObject()
        json.put("start", formatter.format(request.start))
        json.put("end", formatter.format(request.end))
        json.put("type", request.type)
        json.put("n", request.n)
        json.put("value", request.value)
        json.put("valueList", request.valueList)

        val jsonString = json.toString()

        val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
        val iv = ByteArray(cipher.blockSize)
        SecureRandom().nextBytes(iv)

        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encryptedRequest = Base64.encodeToString(cipher.doFinal(jsonString.toByteArray()), Base64.DEFAULT)

        val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
        val keyBytes: ByteArray = Base64.decode(
            request.nextUser!!
                .replace("-----BEGIN PUBLIC KEY-----\n", "")
                .replace("\n-----END PUBLIC KEY-----", ""), Base64.DEFAULT
        )
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")

        val publicKey = keyFactory.generatePublic(spec)
        val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedSynchronousKey = Base64.encodeToString(rsaCipher.doFinal(key.encoded), Base64.DEFAULT)
        return AggregationResponse(
            request.serverId,
            request.nextUser,
            encryptedSynchronousKey,
            encryptedRequest,
            ivString
        )
    }
}