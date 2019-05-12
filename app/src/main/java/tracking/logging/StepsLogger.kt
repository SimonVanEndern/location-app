package tracking.logging

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import tracking.StepsRepository
import tracking.database.LocationRoomDatabase
import tracking.database.schemata.StepsRaw
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class StepsLogger(private val context: Context) : Runnable, SensorEventListener {

    var sensorManager: SensorManager? = null
    private var lastTimeStamp : Long? = null
    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    private val stepsDao = LocationRoomDatabase.getDatabase(context, scope).stepsDao()
    private val stepsRawDao = LocationRoomDatabase.getDatabase(context, scope).stepsRawDao()
    private val stepsRepository = StepsRepository(stepsDao, stepsRawDao)

    override fun run() {

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor != null) {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_NORMAL)
//            Toast.makeText(context, "registered listener", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Cannot register step sensor listener", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.d("STEPS_SENSOR", "last timestamp: $lastTimeStamp")
        if (lastTimeStamp == null || Date().time - lastTimeStamp!! > 1000 * 60 * 1) {
            lastTimeStamp = Date().time
            scope.launch(Dispatchers.IO) {
                //            Log.d("STEPS", "step sensor triggered")
                stepsRepository.insert(StepsRaw(Date().time, Date(), event.values[0].toInt(), false))
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}