package com.example.roomwordsample.database

import android.app.Activity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Database(
    entities = [
        GPSLocation::class,
        GPSData::class,
        ActivityTransition::class,
        Trajectory::class,
        Steps::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class LocationRoomDatabase : RoomDatabase() {

    private class LocationRoomDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.activityDao())
                }
            }
        }

        fun populateDatabase(activityTransitionDao: ActivityTransitionDao) {
//            wordDao.deleteAll()

            val activity = ActivityTransition(0, Date(), 5, 0, 1100)
            activityTransitionDao.insert(activity)
        }
    }

    abstract fun gPSLocationDao(): GPSLocationDao

    abstract fun gPSDataDao(): GPSDataDao

    abstract fun activityDao(): ActivityTransitionDao

    abstract fun trajectoryDao(): TrajectoryDao

    abstract fun stepsDao(): StepsDao

    companion object {
        @Volatile
        private var INSTANCE: LocationRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): LocationRoomDatabase {
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
                            database.execSQL("""CREATE TABLE activity_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    day INTEGER,
                    activity_type INTEGER NOT NULL,
                    transition_type INTEGER NOT NULL,
                    start INTEGER NOT NULL
                    )""")
                            database.execSQL("""INSERT INTO activity_table (id, day, activity_type, transition_type, start)
                    SELECT id, day, activity, 0, start
                    FROM activity_table_old""")
                            database.setTransactionSuccessful()
                        } finally {
                            database.endTransaction()
                        }
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocationRoomDatabase::class.java,
                    "Location_database"
                ).addCallback(LocationRoomDatabaseCallback(scope))
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}