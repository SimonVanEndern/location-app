package com.simonvanendern.tracking.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.database.DatabaseTest
import com.simonvanendern.tracking.database.schemata.StepsDao
import com.simonvanendern.tracking.database.schemata.StepsRaw
import com.simonvanendern.tracking.database.schemata.StepsRawDao
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.*

@RunWith(AndroidJUnit4::class)
class StepsRepositoryTest : DatabaseTest() {

    @Mock
    lateinit var stepsDao: StepsDao

    @Mock
    lateinit var stepsRawDao: StepsRawDao

    lateinit var stepsRepository: StepsRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        stepsRepository = StepsRepository(stepsDao, stepsRawDao)
    }

    @Test
    fun testGet10RecentSteps() {
        stepsRepository.recentSteps

        Mockito.verify(stepsDao).get10RecentSteps()
    }

    @Test
    fun testInsertRawSteps() {
        val stepsRaw = StepsRaw(100, Date(), 32, false)

        runBlocking { stepsRepository.insert(stepsRaw) }

        Mockito.verify(stepsRawDao).insert(stepsRaw)
    }
}