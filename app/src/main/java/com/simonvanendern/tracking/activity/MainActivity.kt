package com.simonvanendern.tracking.activity


import android.Manifest
import android.content.Context
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.simonvanendern.tracking.*
import com.simonvanendern.tracking.backgroundService.BackgroundLoggingService
import com.simonvanendern.tracking.repository.RequestRepository
import java.security.KeyPairGenerator
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val ACTIVITY_REQUEST_CODE = 777
    private val newWordActivityRequestCode = 1
    private lateinit var allDataViewModel: AllDataViewModel

    @Inject
    lateinit var requestRepository: RequestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(applicationContext))
            .build()
            .inject(this)

        setUpApp()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = StepsAndActivityListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get a new or existing ViewModel from the ViewModelProvider.
        allDataViewModel = ViewModelProviders.of(this).get(AllDataViewModel::class.java)


        // Add an observer on the LiveData returned by getAlphabetizedWords.
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

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewWordActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
        }

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

    private fun setUpApp() {
        val store = getSharedPreferences(getString(R.string.identifiers), Context.MODE_PRIVATE)
        if (!store.contains(getString(R.string.public_key))) {
            val generator = KeyPairGenerator.getInstance("RSA")
            val keyPair = generator.genKeyPair()
            with(store.edit()) {
                putString(getString(R.string.public_key), keyPair.public.toString())
                putString(getString(R.string.private_key), keyPair.private.toString())
                apply()
            }
            val user = requestRepository.createUser(
                store.getString(getString(R.string.public_key), null)!!
            )
            if (user != null) {
                with(store.edit()) {
                    putString(getString(R.string.password), user.pw)
                    apply()
                }
            }
        }
    }

    private fun startBackgroundService() {
        val i = Intent(this, BackgroundLoggingService::class.java)
        startService(i)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        Toast.makeText(
            applicationContext,
            R.string.empty_not_saved,
            Toast.LENGTH_LONG
        ).show()
    }

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
