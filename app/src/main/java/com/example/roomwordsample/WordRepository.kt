package com.example.roomwordsample

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.example.roomwordsample.database.Word
import com.example.roomwordsample.database.WordDao

class WordRepository(private val wordDao: WordDao) {
    val allWords: LiveData<List<Word>> = wordDao.getAllWords()

    @WorkerThread
    fun insert(word: Word) {
        wordDao.insert(word)
    }
}