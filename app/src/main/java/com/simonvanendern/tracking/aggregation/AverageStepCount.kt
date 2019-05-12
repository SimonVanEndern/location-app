package com.simonvanendern.tracking.aggregation

import java.util.*

class AverageStepCount(private val startDate1: Date, private val endDate1: Date) : Datatype(startDate1, endDate1) {


    override fun getValue(): Float {
        // TODO: Check what we do if there is no value
        return getDb()?.stepsDao()?.getAverageSteps(startDate1, endDate1) ?: 0.0F
    }
}