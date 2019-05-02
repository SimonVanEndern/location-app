package com.example.roomwordsample.logging

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import com.example.roomwordsample.StepsRepository
import com.example.roomwordsample.database.LocationRoomDatabase
import com.example.roomwordsample.database.schemata.Steps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class StepsLogger(private val context: Context) : Runnable, SensorEventListener {

    var sensorManager: SensorManager? = null
    private var parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    private val stepsDao = LocationRoomDatabase.getDatabase(context, scope).stepsDao()
    private val stepsRepository = StepsRepository(stepsDao)

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
        scope.launch(Dispatchers.IO) {
//            Log.d("STEPS", "step sensor triggered")
            stepsRepository.insert(Steps(Date(), event.values[0].toInt()))
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}