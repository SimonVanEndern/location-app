package com.example.roomwordsample


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roomwordsample.logging.LoggingService
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1
    private lateinit var allDataViewModel: AllDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        allDataViewModel.mostRecentActivity.observe(this, Observer { activities ->
            // Update the cached copy of the words in the adapter.
            activities?.let { adapter.setActivities(it) }
        })

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewWordActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
        }

        val i = Intent(this, LoggingService::class.java)
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
}
