package com.simonvanendern.tracking.database.schemata

import com.simonvanendern.tracking.database.DatabaseTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AggregationRequestDaoTest : DatabaseTest() {

    private lateinit var aggregationRequestDao: AggregationRequestDao

    @Before
    fun init() {
        aggregationRequestDao = getDb().aggregationRequestDao()
    }

    @Test
    fun testActivitySimpleInsert() {
        val serverSideID = "jkdjLLIDJFoi298"
        val data = "11111111111111".toByteArray()

        val aggregationRequest = AggregationRequest(0, serverSideID, data)

        val id = aggregationRequestDao.insert(aggregationRequest)

        val savedAggregationRequest = aggregationRequestDao.getById(id.toInt())

        Assert.assertEquals(serverSideID, savedAggregationRequest.serverId)
        Assert.assertArrayEquals(data, savedAggregationRequest.data)
    }

    @Test
    fun testGetAll() {
        val serverSideId_0 = "iwoijff0293"
        val serverSideId_1 = "oiwiejfoidf"
        val serverSideId_2 = "9w8fu8weijf"

        val data_0 = "000000000".toByteArray()
        val data_1 = "010101010".toByteArray()
        val data_2 = "101010101".toByteArray()

        val aggregationRequest_0 = AggregationRequest(0, serverSideId_0, data_0)
        val aggregationRequest_1 = AggregationRequest(0, serverSideId_1, data_1)
        val aggregationRequest_2 = AggregationRequest(0, serverSideId_2, data_2)

        aggregationRequestDao.insert(aggregationRequest_0)
        aggregationRequestDao.insert(aggregationRequest_1)
        aggregationRequestDao.insert(aggregationRequest_2)

        val savedRequests = aggregationRequestDao.getAll()

        Assert.assertEquals(savedRequests.size, 3)
        Assert.assertTrue(savedRequests.map(AggregationRequest::serverId).contains(serverSideId_0))
        Assert.assertTrue(savedRequests.map(AggregationRequest::serverId).contains(serverSideId_1))
        Assert.assertTrue(savedRequests.map(AggregationRequest::serverId).contains(serverSideId_2))
    }
}

