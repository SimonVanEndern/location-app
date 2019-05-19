package com.simonvanendern.tracking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simonvanendern.tracking.database.schemata.aggregated.Activity
import com.simonvanendern.tracking.database.schemata.raw.ActivityTransition
import com.simonvanendern.tracking.database.schemata.raw.GPSData
import com.simonvanendern.tracking.database.schemata.aggregated.Steps
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class StepsAndActivityListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<StepsAndActivityListAdapter.StepsAndActivityViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var activities = emptyList<String>() // Cached copy of words
    private var steps = emptyList<String>()
    private var locations = emptyList<String>()
    private var activityTransitions = emptyList<String>()

    private val dateFormat = SimpleDateFormat("HH:mm - dd MMM yyyy")
    private val dateFormatWithSeconds = SimpleDateFormat("HH:mm:ss - dd MMMM yyyy")


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
        this.activities = activities.map { activity ->
            val type =
                if (activity.activityType == 7) "WALKING" else if (activity.activityType == 3) "STILL" else "Type " + activity.activityType
            "Activity:\nserverId = ${activity.id}\n" +
                    "start = ${dateFormat.format(Date(activity.start))}\n" +
                    "transitionType = $type\n" +
                    "duration = ${activity.duration / 3600000}h " +
                    "${(activity.duration % 3600000) / 60000}m" +
                    " ${activity.duration % 60000 / 1000}s"
        }
        notifyDataSetChanged()
    }

    internal fun setSteps(steps: List<Steps>) {
        this.steps = steps.map { step ->
            "Step:\n" +
                    "start = " + dateFormat.format(Date(step.timestamp)) + "\n" +
                    "count = " + step.steps
        }.reversed()
        notifyDataSetChanged()
    }

    internal fun setLocations(locations: List<GPSData>) {
        this.locations = locations.map { gps ->
        "GPSData: \n" +
                "serverId = ${gps.location_id}\n" +
                "time =" + dateFormatWithSeconds.format(Date(gps.timestamp))}.reversed()
        notifyDataSetChanged()
    }

    internal fun setActivityTransitions(activityTransitions: List<ActivityTransition>) {
        this.activityTransitions = activityTransitions.map { transition ->
            val type =
                if (transition.activityType == 7) "WALKING" else if (transition.activityType == 3) "STILL" else "Type " + transition.activityType
            val transitionType = if (transition.transitionType == 0) "ENTER" else "EXIT"
            "ActivityTransition: \n" +
                    "serverId = ${transition.id}\n" +
                    "start = ${dateFormat.format(Date(transition.start))}\n" +
                    "activity = $type\n" +
                    "transitionType = $transitionType\n" +
                    "processed = ${transition.processed}"
        }.reversed()
        notifyDataSetChanged()
    }

    override fun getItemCount() = steps.size + activities.size + locations.size + activityTransitions.size
}


