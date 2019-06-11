package com.simonvanendern.tracking.data_collection

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.simonvanendern.tracking.ApplicationModule
import com.simonvanendern.tracking.DaggerApplicationComponent
import com.simonvanendern.tracking.database.data_model.raw.StepsRaw
import com.simonvanendern.tracking.repository.StepsRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Requesting the step counter value from the phone internal step sensor
 */
class StepsLogger(private val context: Context) : Runnable, SensorEventListener {

    var sensorManager: SensorManager? = null
    private var lastTimeStamp = 0L

    @Inject
    lateinit var stepsRepository: StepsRepository

    /**
     * Invoking dependency injection and registering the step sensor listener if the
     * sensor is available
     */
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

    /**
     * Triggered each time the sensor reports new data
     */
    override fun onSensorChanged(event: SensorEvent) {
        val now = Date()
        // We only save steps data at a 60 seconds interval
        if (now.time - lastTimeStamp > 1000 * 60) {
            lastTimeStamp = now.time
            GlobalScope.launch {
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
    }

    // Not needed but has to be implemented
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}