package com.example.roomwordsample.database

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class DatabaseAggregator(private val appContext: Context, workParams: WorkerParameters) :
    Worker(appContext, workParams) {

    private var parentJob = Job()
    // By default all the coroutines launched in this scope should be using the Main dispatcher
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    override fun doWork(): Result {
        val database = LocationRoomDatabase.getDatabase(appContext, scope)

        val cursor = database.openHelper.readableDatabase.query(
            "SELECT MAX(start) FROM activity_transition_table"
        )
        cursor.moveToFirst()
        val index = cursor.getColumnIndexOrThrow("MAX(start)")
        val lastTimestamp = cursor.getString(index)

        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO activity_table (day, activity_type, start, duration)
            SELECT at1.day, at1.activity_type, at1.start, at2.start - at1.start
            FROM activity_transition_table at1, activity_transition_table at2
            WHERE at1.processed = 0
            AND at1.transition_type = 0
            AND at1.activity_type = at2.activity_type
            AND at1.transition_type != at2.transition_type
            AND at2.start = (SELECT MIN(start) FROM activity_transition_table WHERE start > at1.start)
        """
        )

        database.openHelper.writableDatabase.execSQL(
            """UPDATE activity_transition_table SET processed = 1
                WHERE start <= $lastTimestamp
            """
        )

        Log.d("AGGREGATOR", "Successful aggregation")

        return Result.success()
    }
}