package com.simonvanendern.tracking.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simonvanendern.tracking.database.data_model.AggregationRequest
import com.simonvanendern.tracking.database.data_model.AggregationRequestDao
import com.simonvanendern.tracking.database.data_model.aggregated.*
import com.simonvanendern.tracking.database.data_model.raw.*

/**
 * The database used for the tables defined in the model data_model package.
 * All tables have to be specified via the entities attribute.
 * All TypeConverters to be used have to be specified as argument to the TypeConverter annotation.
 * For all data access object classes an abstract function must be present.
 * On compilation, Room generates the respective implemented methods.
 */
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