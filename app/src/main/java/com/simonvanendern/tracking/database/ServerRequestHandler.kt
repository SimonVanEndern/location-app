package com.simonvanendern.tracking.database

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simonvanendern.tracking.database.schemata.Steps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext

class ServerRequestHandler(private val appContext: Context, workParams: WorkerParameters) :
    Worker(appContext, workParams) {

    private var parentJob = Job()
    // By default all the coroutines launched in this scope should be using the Main dispatcher
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private val database = TrackingDatabase.getDatabase(appContext, scope)

    private var formatter = SimpleDateFormat("yyyy-MM-dd")

    override fun doWork(): Result {
        aggregateSteps()

        Log.d("SERVER_HANDLER", "Successful server handling")

        return Result.success()
    }

//    private fun updatePendingRequests () {
//        val requestRepository = RequestRepository(database.aggregationRequestDao())
//    }

    private fun aggregateSteps() {
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT MAX(timestamp) FROM step_counter_table"
        )
        cursor.moveToFirst()
        val index = cursor.getColumnIndexOrThrow("MAX(timestamp)")
        val lastTimestamp = cursor.getString(index)

        if (lastTimestamp != null) {

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
                val test = cursor.columnNames
                val timestamp = cursor.getColumnIndexOrThrow("timestamp")
                val day = cursor.getColumnIndexOrThrow("day")
                val steps = cursor.getColumnIndexOrThrow("steps")
                do {
                    val t = cursor.getLong(timestamp)
                    val d = cursor.getString(day)
                    val s = cursor.getInt(steps)

                    database.stepsDao().insert(
                        Steps(t, formatter.parse(d), s))
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