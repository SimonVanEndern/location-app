package com.simonvanendern.tracking.aggregation

import com.simonvanendern.tracking.database.TrackingDatabase
import java.util.*


abstract class Datatype constructor(val startDate: Date, val endDate: Date) {

    // TODO: Replace with correct database access code
    private val db: TrackingDatabase? = null

    abstract fun getValue(): Float

    fun getDb(): TrackingDatabase? {
        return db
    }
}