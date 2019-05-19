package com.simonvanendern.tracking.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simonvanendern.tracking.database.schemata.AggregationRequest
import com.simonvanendern.tracking.database.schemata.AggregationRequestDao
import com.simonvanendern.tracking.database.schemata.aggregated.*
import com.simonvanendern.tracking.database.schemata.raw.*

@Database(
    entities = [
        GPSLocation::class,
        GPSData::class,
        ActivityTransition::class,
        Trajectory::class,
        StepsRaw::class,
        Steps::class,
        Activity::class,
        AggregationRequest::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class TrackingDatabase : RoomDatabase() {

    abstract fun gPSLocationDao(): GPSLocationDao

    abstract fun gPSDataDao(): GPSDataDao

    abstract fun activityDao(): ActivityDao

    abstract fun activityTransitionDao(): ActivityTransitionDao

    abstract fun trajectoryDao(): TrajectoryDao

    abstract fun stepsDao(): StepsDao

    abstract fun stepsRawDao(): StepsRawDao

    abstract fun aggregationRequestDao(): AggregationRequestDao
}