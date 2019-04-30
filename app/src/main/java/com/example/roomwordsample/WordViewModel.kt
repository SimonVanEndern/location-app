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

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.roomwordsample.database.Activity
import com.example.roomwordsample.database.LocationRoomDatabase
import com.example.roomwordsample.database.Word
import com.example.roomwordsample.database.WordRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * View Model to keep a reference to the word repository and
 * an up-to-date list of all words.
 */

class WordViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()
    // By default all the coroutines launched in this scope should be using the Main dispatcher
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val repository: WordRepository
    private val stepsRepository : StepsRepository
    private val activityRepository : ActivityRepository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allWords: LiveData<List<Word>>
    val mostRecentSteps : LiveData<List<Int>>
    val mostRecentActivity : LiveData<List<Activity>>

    init {
        val wordsDao = WordRoomDatabase.getDatabase(application, scope).wordDao()
        val stepsDao = LocationRoomDatabase.getDatabase(application, scope).stepsDao()
        val activityDao = LocationRoomDatabase.getDatabase(application, scope).activityDao()
        stepsRepository = StepsRepository(stepsDao)
        activityRepository = ActivityRepository(activityDao)
        repository = WordRepository(wordsDao)
        allWords = repository.allWords
        mostRecentSteps = stepsRepository.recentSteps
        mostRecentActivity = activityRepository.recentActivities
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(word: Word) = scope.launch(Dispatchers.IO) {
        repository.insert(word)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
