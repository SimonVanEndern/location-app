package com.simonvanendern.tracking


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simonvanendern.tracking.backgroundService.BackgroundService

/**
 * Main Activity showing some debug data
 */
class MainActivity : AppCompatActivity() {

    private val ACTIVITY_REQUEST_CODE = 777
    private lateinit var allDataViewModel: AllDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = DebugDataAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get a new or existing ViewModel from the ViewModelProvider.
        allDataViewModel = ViewModelProviders.of(this).get(AllDataViewModel::class.java)


        // Add an observer on the LiveData.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        allDataViewModel.mostRecentSteps.observe(this, Observer { steps ->
            // Update the cached copy of the words in the adapter.
            steps?.let { adapter.setSteps(it) }
        })

        allDataViewModel.mostRecentActivities.observe(this, Observer { activities ->
            // Update the cached copy of the words in the adapter.
            activities?.let { adapter.setActivities(it) }
        })

        allDataViewModel.mostRecentLocations.observe(this, Observer { location ->
            location?.let { adapter.setLocations(it) }
        })

        allDataViewModel.mostRecentActivityTransitions.observe(this, Observer { activityTransition ->
            activityTransition?.let { adapter.setActivityTransitions(it) }
        })

        // Ask the user for the permission to access its location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACTIVITY_REQUEST_CODE
            )
        } else {
            startBackgroundService()
        }

    }

    /**
     * Starts the service controlling all background tasks
     */
    private fun startBackgroundService() {
        val i = Intent(this, BackgroundService::class.java)
        startService(i)
    }

    /**
     * Handles the response (denial or approval) of the user to the location access request
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ACTIVITY_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBackgroundService()

                } else {
                    Toast.makeText(this, "Sorry, cannot run without location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}