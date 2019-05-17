package com.simonvanendern.tracking.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConvertersTest {

    @Ignore
    @Test
    fun testConversionToJson() {
//        val steps = AverageStepCount(Date(), Date())
//        val request = AverageAggregationRequest(steps, 1, 1.0f)

//        val result = Converters().aggregationRequestToJson<AverageStepCount>(request)

        val expectedResult = "{\n" +
                "  \"n\": 1,\n" +
                "  \"t\": {\n" +
                "    \"endDate1\": \"May 17, 2019 13:00:19\",\n" +
                "    \"startDate1\": \"May 17, 2019 13:00:19\",\n" +
                "    \"endDate\": \"May 17, 2019 13:00:19\",\n" +
                "    \"startDate\": \"May 17, 2019 13:00:19\"\n" +
                "  },\n" +
                "  \"value\": 1\n" +
                "}"

//        Assert.assertEquals("", result)
    }
}