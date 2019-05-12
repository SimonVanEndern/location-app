package com.simonvanendern.tracking

import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simonvanendern.tracking.logging.ActivityRecognitionReceiver
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ActivityRecognitionReceiverTest {

    private var activityRecognitionReceiver: ActivityRecognitionReceiver? = null
    private var mContext: Context? = null

    // Executes each task synchronously using Architecture Components.
    @get :Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        activityRecognitionReceiver = ActivityRecognitionReceiver()
        mContext = mock(Context::class.java)
    }

    @Ignore("Not possible to properly inject a mocked object")
    @Test
    fun testReceiverAllIntent() {
        val intent = Intent(mContext, ActivityRecognitionReceiver::class.java)

        activityRecognitionReceiver!!.onReceive(mContext!!, intent)
    }
}