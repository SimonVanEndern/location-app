package com.example.roomwordsample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.roomwordsample.database.Activity
import com.example.roomwordsample.database.ActivityTransition
import com.example.roomwordsample.database.GPSData


class StepsAndActivityListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<StepsAndActivityListAdapter.StepsAndActivityViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var activities = emptyList<String>() // Cached copy of words
    private var steps = emptyList<String>()
    private var locations = emptyList<String>()
    private var activityTransitions = emptyList<String>()

    inner class StepsAndActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stepsAndActivityItemView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepsAndActivityViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return StepsAndActivityViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StepsAndActivityViewHolder, position: Int) {
        val combined = ArrayList<String>()
        combined.addAll(steps)
        combined.addAll(activityTransitions)
        combined.addAll(activities)
        combined.addAll(locations)
        val current = combined[position]
        holder.stepsAndActivityItemView.text = current
    }

    internal fun setActivities(activities: List<Activity>) {
        this.activities = activities.map(Activity::toString)
        notifyDataSetChanged()
    }

    internal fun setSteps(steps: List<Int>) {
        this.steps = steps.map { step -> "" + step }
        notifyDataSetChanged()
    }

    internal fun setLocations(locations: List<GPSData>) {
        this.locations = locations.map(GPSData::toString)
        notifyDataSetChanged()
    }

    internal fun setActivityTransitions(activityTransitions: List<ActivityTransition>) {
        this.activityTransitions = activityTransitions.map(ActivityTransition::toString)
        notifyDataSetChanged()
    }

    override fun getItemCount() = steps.size + activities.size + locations.size + activityTransitions.size
}


