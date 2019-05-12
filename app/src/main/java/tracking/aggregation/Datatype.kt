package tracking.aggregation

import tracking.database.LocationRoomDatabase
import java.util.*


abstract class Datatype constructor(val startDate: Date, val endDate: Date) {

    // TODO: Replace with correct database access code
    private val db: LocationRoomDatabase? = null

    abstract fun getValue(): Float

    fun getDb(): LocationRoomDatabase? {
        return db
    }
}