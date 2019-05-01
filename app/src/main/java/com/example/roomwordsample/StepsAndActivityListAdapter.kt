package com.example.roomwordsample

/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.roomwordsample.database.Activity


class StepsAndActivityListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<StepsAndActivityListAdapter.StepsAndActivityViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var activities = emptyList<String>() // Cached copy of words
    private var steps = emptyList<String>()

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
        combined.addAll(activities)
        val current = combined[position]
        holder.stepsAndActivityItemView.text = current
    }

    internal fun setActivities(activities: List<Activity>) {
        this.activities = activities.map { activity -> activity.toString() }
        notifyDataSetChanged()
    }

    internal fun setSteps(steps: List<Int>) {
        this.steps = steps.map { step -> "" + step }
        notifyDataSetChanged()
    }

    override fun getItemCount() = steps.size + activities.size
}


