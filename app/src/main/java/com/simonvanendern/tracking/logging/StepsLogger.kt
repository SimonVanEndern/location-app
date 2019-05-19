package com.simonvanendern.tracking.logging

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.simonvanendern.tracking.ApplicationModule
import com.simonvanendern.tracking.DaggerApplicationComponent
import com.simonvanendern.tracking.database.schemata.raw.StepsRaw
import com.simonvanendern.tracking.repository.StepsRepository
import java.util.*
import javax.inject.Inject

class StepsLogger(private val context: Context) : Runnable, SensorEventListener {

    var sensorManager: SensorManager? = null
    private var lastTimeStamp = 0L

    @Inject
    lateinit var stepsRepository: StepsRepository

    override fun run() {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(context))
            .build()
            .inject(this)

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor != null) {
            sensorManager?.registerListener(
                this,
                stepsSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.d("STEPS_SENSOR", "last timestamp: $lastTimeStamp")
        val now = Date()
        if (now.time - lastTimeStamp > 1000 * 60 * 1) {
            lastTimeStamp = now.time
            stepsRepository.insert(
                StepsRaw(
                    now.time,
                    now,
                    event.values[0].toInt(),
                    false
                )
            )
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}