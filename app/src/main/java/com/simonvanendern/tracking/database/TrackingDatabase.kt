package com.simonvanendern.tracking.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simonvanendern.tracking.database.schemata.AggregationRequest
import com.simonvanendern.tracking.database.schemata.AggregationRequestDao
import com.simonvanendern.tracking.database.schemata.aggregated.*
import com.simonvanendern.tracking.database.schemata.raw.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    private class LocationRoomDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.gPSLocationDao(), database.gPSDataDao())
                }
            }
        }

        fun populateDatabase(gpsLocationDao: GPSLocationDao, gpsDataDao: GPSDataDao) {
//            wordDao.deleteAll()
            Log.d("DATABASE", "populated")
            val location = GPSLocation(0, 1.1F, 1.2F, 2.0F)
            val id = gpsLocationDao.insert(location)
//            gpsDataDao.insert(GPSData(id, 12345))
        }
    }

    abstract fun gPSLocationDao(): GPSLocationDao

    abstract fun gPSDataDao(): GPSDataDao

    abstract fun activityDao(): ActivityDao

    abstract fun activityTransitionDao(): ActivityTransitionDao

    abstract fun trajectoryDao(): TrajectoryDao

    abstract fun stepsDao(): StepsDao

    abstract fun stepsRawDao(): StepsRawDao

    abstract fun aggregationRequestDao(): AggregationRequestDao

    companion object {
        @Volatile
        private var INSTANCE: TrackingDatabase? = null

        fun setDatabase(db: TrackingDatabase) {
            INSTANCE = db
        }

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): TrackingDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val MIGRATION_1_2 = object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.beginTransaction()
                        try {
                            database.execSQL("ALTER TABLE activity_table RENAME TO activity_table_old;")
                            database.execSQL(
                                """CREATE TABLE activity_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    day INTEGER,
                    activity_type INTEGER NOT NULL,
                    transition_type INTEGER NOT NULL,
                    start INTEGER NOT NULL
                    )"""
                            )
                            database.execSQL(
                                """INSERT INTO activity_table (id, day, activity_type, transition_type, start)
                    SELECT id, day, activity, 0, start
                    FROM activity_table_old"""
                            )
                            database.setTransactionSuccessful()
                        } finally {
                            database.endTransaction()
                        }
                    }
                }

                val MIGRATION_2_3 = object : Migration(2, 3) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE activity_table ADD COLUMN processed INTEGER NOT NULL DEFAULT 0")
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackingDatabase::class.java,
                    "Location_database"
                ).addCallback(LocationRoomDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}