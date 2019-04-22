package com.example.roomwordsample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.roomwordsample.database.Word
import com.example.roomwordsample.database.WordDao
import com.example.roomwordsample.database.WordRoomDatabase
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class WordDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var wordDao: WordDao
    private lateinit var db: WordRoomDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, WordRoomDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        wordDao = db.wordDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetWord() {
        val word = Word("word")
        wordDao.insert(word)
        val allWords = wordDao.getAllWords().waitForValue()
        Assert.assertEquals(allWords[0].word, word.word)
    }

    @Test
    @Throws(Exception::class)
    fun getAllWords() {
        val word = Word("aaa")
        wordDao.insert(word)
        val word2 = Word("bbb")
        wordDao.insert(word2)
        val allWords = wordDao.getAllWords().waitForValue()
        Assert.assertEquals(allWords[0].word, word.word)
        Assert.assertEquals(allWords[1].word, word2.word)
    }

    @Test
    @Throws(Exception::class)
    fun deleteAll() {
        val word = Word("word")
        wordDao.insert(word)
        val word2 = Word("word2")
        wordDao.insert(word2)
        wordDao.deleteAll()
        val allWords = wordDao.getAllWords().waitForValue()
        Assert.assertTrue(allWords.isEmpty())
    }
}