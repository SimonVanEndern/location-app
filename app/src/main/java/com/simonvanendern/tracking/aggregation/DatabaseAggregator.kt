package com.simonvanendern.tracking.aggregation

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.database.schemata.Steps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext

class DatabaseAggregator(private val appContext: Context, workParams: WorkerParameters) :
    Worker(appContext, workParams) {

    private var parentJob = Job()
    // By default all the coroutines launched in this scope should be using the Main dispatcher
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private val database =
        TrackingDatabase.getDatabase(appContext, scope)

    private var formatter = SimpleDateFormat("yyyy-MM-dd")

    override fun doWork(): Result {
        aggregateSteps()
        aggregateActivities()

        Log.d("AGGREGATOR", "Successful aggregation")

        return Result.success()
    }

    private fun aggregateActivities() {
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
            WHERE at2.processed = 0
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
    }

    private fun aggregateSteps() {
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT MAX(timestamp) FROM step_counter_table"
        )
        cursor.moveToFirst()
        val index = cursor.getColumnIndexOrThrow("MAX(timestamp)")
        val lastTimestamp = cursor.getString(index)

        if (lastTimestamp != null) {

            val firstValue = database.openHelper.readableDatabase.query(
                """SELECT *
                    FROM step_counter_table
                    WHERE timestamp =
                    (SELECT MIN(timestamp) FROM step_counter_table)
                    LIMIT 1"""
            )
            firstValue.moveToFirst()

            val index = firstValue.getColumnIndexOrThrow("processed")
            val processed = firstValue.getInt(index)
            println(processed)
            if (processed == 0) {
                val dayIndex = firstValue.getColumnIndexOrThrow("day")
                val day = firstValue.getString(dayIndex)
                val timestampIndex = firstValue.getColumnIndexOrThrow("timestamp")
                val timestamp = firstValue.getLong(timestampIndex)
                val stepsIndex = firstValue.getColumnIndexOrThrow("steps")
                val steps = firstValue.getInt(stepsIndex)
                database.stepsDao().insert(Steps(timestamp, formatter.parse(day), steps))
            }

            val cursor = database.openHelper.writableDatabase.query(
                """
            SELECT s1.timestamp, s1.day, s2.steps - s1.steps AS steps
            FROM step_counter_table AS s1, step_counter_table AS s2
            WHERE s2.processed = 0
            AND s2.steps > s1.steps
            AND s2.timestamp =
            (SELECT MIN(timestamp) FROM step_counter_table WHERE timestamp > s1.timestamp)
            UNION
            SELECT s1.timestamp, s1.day, s2.steps
            FROM step_counter_table AS s1, step_counter_table AS s2
            WHERE s2.processed = 0
            AND s2.steps < s1.steps
            AND s2.timestamp =
            (SELECT MIN(timestamp) FROM step_counter_table WHERE timestamp > s1.timestamp);
        """
            )

            if (cursor.moveToFirst()) {
                val timestamp = cursor.getColumnIndexOrThrow("timestamp")
                val day = cursor.getColumnIndexOrThrow("day")
                val steps = cursor.getColumnIndexOrThrow("steps")
                do {
                    val t = cursor.getLong(timestamp)
                    val d = cursor.getString(day)
                    val s = cursor.getInt(steps)

                    database.stepsDao().insert(
                        Steps(t, formatter.parse(d), s)
                    )
                } while (cursor.moveToNext())
            }

            database.openHelper.writableDatabase.execSQL(
                """UPDATE step_counter_table SET processed = 1
                    WHERE timestamp <= $lastTimestamp;
            """
            )
        }
    }
}